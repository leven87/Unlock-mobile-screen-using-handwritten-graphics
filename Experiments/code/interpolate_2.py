import scipy.interpolate
import numpy as np, matplotlib.pyplot as plt
from scipy.interpolate import interp1d
# x= np.array([0, 1, 2, 3, 4, 5, 6, 7])
# y= np.array([3, 4, 3.5, 2, 1, 1.5, 1.25, 0.9])
# x= np.array(list(range(0,len(data1), 1)))
print(data1['TStamp2'])
x= np.array(data1['TStamp2'].tolist())
y= np.array(data1['normalX'].tolist())

xx = np.linspace(x.min(), x.max(), 100)
fig, ax = plt.subplots(figsize=(20, 10))
ax.scatter(x, y)
# for n in ['linear','zero', 'slinear', 'quadratic', 'cubic']:
for n in ['linear','cubic']:    
    f = interp1d(x, y, kind = n)
    ax.plot(xx, f(xx), label= n)

# plt.plot( xx, f(xx),'c*-')
    
ax.legend()
ax.set_ylabel(r"$y$", fontsize=18)
ax.set_xlabel(r"$x$", fontsize=18)
plt.show()


# fig = plt.figure(figsize=[20,14])

# plt.plot( list(range(0,len(data1), 1)), data1["normalX"],'c*-')
# fig.show()



# # 进行样条插值
# tck = scipy.interpolate.splrep(x,y)
# xx = np.linspace(min(x),max(x),100)
# yy = scipy.interpolate.splev(xx,tck,der=0)
# print(xx)
# # 画图
# fig = plt.figure(figsize=[20,14])
# plt.plot(x,y,'o',xx,yy)
# # plt.plot( list(range(0,len(data1), 1)), data1["normalX"],'c*-')

# plt.legend(['true','Cubic-Spline'])