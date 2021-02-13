# -*- coding: utf-8 -*-
"""
Created on Mon Apr  1 16:32:14 2019

@author: losdwind
"""

import numpy as np
import pandas as pd
from matplotlib.pyplot import figure, plot, legend, xlabel, show, title,ylabel
from scipy.linalg import svd

# Read Data
df = pd.read_excel('Concrete_Data-Origin.xls')
attributeNames = np.asarray(df.columns[0:-1])
N, M = df.shape
y = np.asarray(df.iloc[:,-1])
X = np.asarray(df.iloc[:, :-1])
classNames = ['Low','Medium','High']
C = len(classNames)
y_c = np.asarray(pd.cut(y,bins = [0,25,40,83], right= False, labels=[0,1,2])).squeeze()

mu = np.mean(X, 0)
sigma = np.std(X, 0)  
X = (X - mu) / sigma

'''
# Subtract mean value from data
Y = X - np.ones((N,1))*X.mean(axis=0)

# PCA by computing SVD of Y
U,S,Vh = svd(Y,full_matrices=False)

# Compute variance explained by principal components
rho = (S*S) / (S*S).sum() 

threshold = 0.9

# Plot variance explained
plt.figure(1)
plt.plot(range(1,len(rho)+1),rho,'x-')
plt.plot(range(1,len(rho)+1),np.cumsum(rho),'o-')
plt.plot([1,len(rho)],[threshold, threshold],'k--')
plt.title('Variance explained by principal components');
plt.xlabel('Principal component');
plt.ylabel('Variance explained');
plt.legend(['Individual','Cumulative','Threshold'])
plt.grid()
plt.show()



# scipy.linalg.svd returns "Vh", which is the Hermitian (transpose)
# of the vector V. So, for us to obtain the correct V, we transpose:
V = Vh.T    

# Project the centered data onto principal component space
Z = Y @ V

# Indices of the principal components to be plotted
i = 0
j = 1

# Plot PCA of the data
f = figure(2)
title('concrete data: PCA')
#Z = array(Z)
for c in range(C):
    # select indices belonging to class c:
    class_mask = y_c==c
    plot(Z[class_mask,i], Z[class_mask,j], 'o', alpha=.5)
legend(classNames)
xlabel('PC{0}'.format(i+1))
ylabel('PC{0}'.format(j+1))

x_pc = 0

# Output result to screen
show()
'''