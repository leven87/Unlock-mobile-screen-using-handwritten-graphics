
def dtw_distance(ts_a, ts_b, d=lambda x,y: abs(x-y), mww=10000):
    """Computes dtw distance between two time series


    Args:
        ts_a: time series a
        ts_b: time series b
        d: distance function
        mww: max warping window, int, optional (default = infinity)
        
    Returns:
        dtw distance
    """
    
    # Create cost matrix via broadcasting with large int
    ts_a, ts_b = np.array(ts_a), np.array(ts_b)
    M, N = len(ts_a), len(ts_b)
    cost = np.ones((M, N))

    # Initialize the first row and column
    cost[0, 0] = d(ts_a[0], ts_b[0])
    for i in range(1, M):
        cost[i, 0] = cost[i-1, 0] + d(ts_a[i], ts_b[0])

    for j in range(1, N):
        cost[0, j] = cost[0, j-1] + d(ts_a[0], ts_b[j])

    # Populate rest of cost matrix within window
    for i in range(1, M):
        for j in range(max(1, i - mww), min(N, i + mww)):
            choices = cost[i-1, j-1], cost[i, j-1], cost[i-1, j]
            cost[i, j] = min(choices) + d(ts_a[i], ts_b[j])

    # Return DTW distance given window
    print (cost)
    return cost[-1, -1]


def do(ts_a, ts_b):
    """Calculate with Euclidean distance.
    Examples
    ---------
    ts_a=np.array([5,4])
    ts_b=np.array([3,2])
    print (do(ts_a, ts_b))

    """
    ts_c = ts_a - ts_b
    ts_c = ts_c[np.newaxis,:]
    osd = np.linalg.norm(ts_c, ord=None, axis=1, keepdims=True)
    return osd
 
def cal_dtw_distance(ts_a, ts_b):
    """Returns the DTW similarity distance between two 2-D
    timeseries numpy arrays.
    Arguments
    ---------
    ts_a, ts_b : array of shape [n_samples, n_timepoints]
        Two arrays containing n_samples of timeseries data
        whose DTW distance between each sample of A and B
        will be compared
    d : DistanceMetric object (default = abs(x-y))
        the distance measure used for A_i - B_j in the
        DTW dynamic programming function
    Returns
    -------
    DTW distance between A and B
    """
    # Create cost matrix via broadcasting with large int
    ts_a, ts_b = np.array(ts_a), np.array(ts_b)
    M, N = len(ts_a), len(ts_b)
    cost = np.ones((M, N))
 
    # Initialize the first row and column
    cost[0, 0] = do(ts_a[0], ts_b[0])
    for i in range(1, M):
        cost[i, 0] = cost[i - 1, 0] + do(ts_a[i], ts_b[0])
 
    for j in range(1, N):
        cost[0, j] = cost[0, j - 1] + do(ts_a[0], ts_b[j])
 
    path = [[1,1]]
    # Populate rest of cost matrix within window
    for i in range(1, M):
        for j in range(1,N):
            choices = cost[i - 1, j - 1], cost[i, j - 1], cost[i - 1, j]
            cost[i, j] = min(choices) + do(ts_a[i], ts_b[j])

    i,j = np.array(cost.shape) - 2
    print(i,j)
    
    #最短路径
    # print i,j
    p,q = [i],[j]
    while(i>0 or j>0):
        tb = np.argmin((cost[i,j],cost[i,j+1],cost[i+1,j]))
        if tb==0 :
            i-=1
            j-=1
        elif tb==1 :
            i-=1
        else:
            j-=1
        p.insert(0,i)
        q.insert(0,j)
                
#     print (list(zip(p,q)))
            
    # Return DTW distance given window
    return cost, cost[-1, -1]


starttime = datetime.datetime.now()

#set ts_a
normalX = np.array(data1['normalX'])
normalY = np.array(data1['normalY'])
list(zip(normalX,normalY))
ts_a = np.array(list(zip(normalX,normalY)),dtype=float)

#set ts_b
normalX = np.array(data2['normalX'])
normalY = np.array(data2['normalY'])
list(zip(normalX,normalY))
ts_b = np.array(list(zip(normalX,normalY)),dtype=float)

#calculate DTW distance
cost,d = cal_dtw_distance(ts_a, ts_b)
endtime = datetime.datetime.now()
# print ((endtime - starttime).seconds
# print (cost[0])
print(d)
              