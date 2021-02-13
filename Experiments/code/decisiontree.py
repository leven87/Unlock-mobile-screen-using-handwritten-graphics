from sklearn import tree, model_selection
from platform import system
from os import getcwd
from toolbox_02450 import windows_graphviz_call
import matplotlib.pyplot as plt
from matplotlib.image import imread
from matplotlib.pylab import figure, plot, xlabel, ylabel, legend, show, boxplot, subplot, grid
from filereader import *

########################################################## Training
# Decision Tree
# Fit regression tree classifier, Gini split criterion, no pruning
'''
criterion='gini'
dtc = tree.DecisionTreeClassifier(criterion=criterion, min_samples_split=2)
'''
criterion='entropy'
dtc = tree.DecisionTreeClassifier(criterion=criterion, min_samples_split=1.0/N)

dtc = dtc.fit(X,y_c)
fname='tree_' + criterion


    
# Export tree graph .gvz file to parse to graphviz
out = tree.export_graphviz(dtc, out_file= fname + '.gvz', feature_names= attributeNames)

'''
# Depending on the platform, we handle the file differently, first for Linux 
# Mac
if system() == 'Linux' or system() == 'Darwin':
    import graphviz
    # Make a graphviz object from the file
    src=graphviz.Source.from_file(fname + '.gvz')
    print('\n\n\n To view the tree, write "src" in the command prompt \n\n\n')
    
# ... and then for Windows:
if system() == 'Windows':
    # N.B.: you have to update the path_to_graphviz to reflect the position you 
    # unzipped the software in!
    windows_graphviz_call(fname=fname,
                          cur_dir=getcwd(),
                          path_to_graphviz=r'C:\Program Files (x86)\Graphviz2.38')
    plt.figure(figsize=(12,12))
    plt.imshow(imread(fname + '.png'))
    plt.box('off'); plt.axis('off')
    plt.show()
'''   
###################################################### Predicting
# Define a new data object (a dragon) with the attributes given in the text
x = np.array([540, 0, 0,162, 2.5, 1055, 676, 28]).reshape(1,-1)

# Evaluate the classification tree for the new data object
x_class = dtc.predict(x)[0]

# Print results
print('\nNew object attributes:')
print(dict(zip(attributeNames,x[0])))
print('\nClassification result:')
print(classNames[x_class])

##################################################### cross-validation-holdout
# Tree complexity parameter - constraint on maximum depth
# K-fold crossvalidation
K = 10
CV = model_selection.KFold(n_splits=K,shuffle=True)

# Initialize variable
Error_train = np.empty((K,1))
Error_test = np.empty((K,1))
opt_depth_list = np.empty((K,1))
k=0
for train_index, test_index in CV.split(X):
    print('Computing CV fold: {0}/{1}..'.format(k+1,K))

    # extract training and test set for current CV fold
    X_train, y_train = X[train_index,:], y_c[train_index]
    X_test, y_test = X[test_index,:], y_c[test_index]
    
    ######################################################## find the best tree depth
    # Tree complexity parameter - constraint on maximum depth
    tc = np.arange(2, 20, 1)
    
    # K-fold crossvalidation
    cvf = 5
    CV = model_selection.KFold(n_splits=cvf,shuffle=True)
    
    # Initialize variable
    Error_train_tc = np.empty((len(tc),cvf))
    Error_test_tc = np.empty((len(tc),cvf))
    
    f = 0
    for train_index_tc, test_index_tc in CV.split(X_train):
        print('Computing CVf fold: {0}/{1}..'.format(f+1,cvf))
    
        # extract training and test set for current CV fold
        X_train_tc, y_train_tc = X_train[train_index_tc,:], y_train[train_index_tc]
        X_test_tc, y_test_tc = X_train[test_index_tc,:], y_train[test_index_tc]
    
        for i, t in enumerate(tc):
            # Fit decision tree classifier, Gini split criterion, different pruning levels
            dtc = tree.DecisionTreeClassifier(criterion='gini', max_depth=t)
            dtc = dtc.fit(X_train_tc,y_train_tc.ravel())
            y_est_test_tc = dtc.predict(X_test_tc)
            y_est_train_tc = dtc.predict(X_train_tc)
            # Evaluate misclassification rate over train/test data (in this CV fold)
            misclass_rate_test_tc = np.sum(y_est_test_tc != y_test_tc) / float(len(y_est_test_tc))
            misclass_rate_train_tc = np.sum(y_est_train_tc != y_train_tc) / float(len(y_est_train_tc))
            Error_test_tc[i,f], Error_train_tc[i,f] = misclass_rate_test_tc, misclass_rate_train_tc
        f+=1
    figure(cvf, figsize=(12,8))
    subplot(1,2,1)
    #print(Error_test_tc.mean(1), Error_train_tc.mean(1) ) 
    boxplot(Error_test_tc.T,)
    xlabel('Model complexity (max tree depth)')
    ylabel('Test error across cvf folds, cvf={0})'.format(K))
    subplot(1,2,2)
    plot(tc, Error_train_tc.mean(1))
    plot(tc, Error_test_tc.mean(1))
    xlabel('Model complexity (max tree depth)')
    ylabel('Error (misclassification rate, cvf={0})'.format(K))
    legend(['Error_train','Error_test'])
    grid()
    show()
    
    opt_val_err = np.min(Error_test_tc.mean(1))
    print('the optimal value of error   :',opt_val_err)
    opt_depth = tc[np.argmin(Error_test_tc.mean(1))]
    print('the optimal depth:   ',opt_depth)
    dtc = tree.DecisionTreeClassifier(criterion='gini', max_depth=opt_depth)
    dtc = dtc.fit(X_train,y_train.ravel())
    y_est_test = dtc.predict(X_test)
    y_est_train = dtc.predict(X_train)
    # Evaluate misclassification rate over train/test data (in this CV fold)
    misclass_rate_test = np.sum(y_est_test != y_test) / float(len(y_est_test))
    misclass_rate_train = np.sum(y_est_train != y_train) / float(len(y_est_train))
    Error_test[k], Error_train[k] = misclass_rate_test, misclass_rate_train
    opt_depth_list[k] = opt_depth
    print('the error of train and test for each outer fold',Error_test[k], Error_train[k])
    k = k + 1
print(Error_test.mean())
print('*****************************')
print(opt_depth_list.T, Error_test.T)
    
