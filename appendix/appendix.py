"""
Run the file with 3 input parameters as:
1) policy:
0: Method-A
1: Method-B

2)traffic parameter
1: IRM
z: hyper-z

3) Download delay (s)
The output will be printed to a text file as (K, hit, time, T_C, C).
"""
import numpy as np
import scipy as sc
import scipy.integrate as integrate
import scipy.stats as stats
import math as math
import sys
import time

###################### Utility Functions ###########################

def isclose(a, b, rel_tol=1e-09, abs_tol=0.0):
    return abs(a-b) <= max(rel_tol * max(abs(a), abs(b)), abs_tol)


def initizipf(alpha, CATALOG):
    sum=0
    k = 1
    for k in range(1,CATALOG+1):
        sum+= np.power(k,-  alpha);
    return sum

def zipfs_pop(fId, alpha):
    return  np.power(fId,-alpha)/sumzipf;


def getCTime():
    if CTime==-1:
        if policy==2:
            return computeDehghanAnalyticalCharacteristicTime()
        else:
            return computeMahdiehAnalyticalCharacteristicTime()
    else:
        return CTime

#F
def CumF( rate, time):
   p = (z / (z + 1));
   zDouble = z
   exp1 = 0.0;
   if math.isinf(np.exp(zDouble * rate * time)):
       exp1 = np.finfo(np.float64).max
        #print(exp1)
   else:
       exp1 = np.exp(zDouble * rate * time)

   exp2 = 0.0
   if math.isinf(np.exp(rate * time / zDouble)):
       exp2 = np.finfo(np.float64).max
   else:
       exp2 = np.exp(rate * time / zDouble)
    #if rate > 360:
    #print("rate=%e\texp1=%e\texp2=%e\tp=%e\ttime=%e" %(rate, exp1 ,exp2, p, time))
   return ((p) - ((p) / exp1)) + ((1 - p) - ((1 - p) / exp2))

#\hat F
def CumG( rate, time):
    p = (z / (z + 1));
    zDouble = z;
    exp1 = 0.0;
    if math.isinf(np.exp(zDouble * rate * time)):
        exp1 = np.finfo(np.float64).max
    else:
        exp1 = np.exp(zDouble * rate * time)
                            
    exp2 = 0.0
    if math.isinf(np.exp(rate * time / zDouble)):
        exp2 = np.finfo(np.float64).max
    else:
        exp2 = np.exp(rate * time / zDouble)
                                                
    return ((1 - p) - ((1 - p) / exp1)) + (p - (p / exp2))


###################### Start Of Method B ###########################

#Formula A.1
def dehghancumG( rate, time):
    p = (z / (z + 1));
    x1 = z * rate;
    x2 = (1.0 / z) * rate;
    B = ((1 - p) * x1 + p * x2);
    secondPart = 0
    if  int(z) != 1 :
        C =  ((p * (1 - p) * np.power((x1 - x2), 2)) / np.power(B, 2))
        exp1 = 0.0;
        if math.isinf(np.exp(x1 * time)):
            exp1 = np.finfo(np.float64).max
        else:
            exp1 = np.exp(x1 * time)
    
        exp2 = 0.0
        if math.isinf(np.exp(x2 * time )):
            exp2 = np.finfo(np.float64).max
        else:
            exp2 = np.exp(x2 * time)
        constant1 = (np.exp(-1 * x1 * theoreticalDownTime) - np.exp(-1 * B * theoreticalDownTime))
        constant2 = (np.exp(-1 * x2 * theoreticalDownTime) - np.exp(-1 * B * theoreticalDownTime))
        secondPart = C * B * (((p/(B-x1))  * constant1 / exp1) + (((1-p)/(B- x2))  * constant2 / exp2))
        
    else :
        C = 0
        secondPart = 0
    #return CumF(rate, theoreticalDownTime+time) - CumG(rate, theoreticalDownTime + time) + CumG(rate, time)
    sum =  (CumF(rate, theoreticalDownTime+time) - CumG(rate, theoreticalDownTime + time) + CumG(rate, time)) - secondPart
    assert sum >= 0
    return sum
    #return CumG(rate, time)


def F( rate, time):
    p = (z / (z + 1));
    zDouble = z
    exp1 = 0.0;
    if math.isinf(np.exp(zDouble * rate * time)):
        exp1 = np.finfo(np.float64).max
    else:
        exp1 = np.exp(zDouble * rate * time)
                            
    exp2 = 0.0
    if math.isinf(np.exp(rate * time / zDouble)):
        exp2 = np.finfo(np.float64).max
    else:
        exp2 = np.exp(rate * time / zDouble)
    #if rate > 360:
    #print("rate=%e\texp1=%e\texp2=%e\tp=%e\ttime=%e" %(rate, exp1 ,exp2, p, time))
    return ((p * zDouble * rate)/ exp1) + (((1 - p)*rate/zDouble) / exp2)

#renewal function
def counting_function(rate, time):
    p = (z / (z + 1));
    x1 = z * rate;
    x2 = (1.0 / z) * rate;
    B = ((1 - p) * x1 + p * x2);
    firstPart = (x1 * x2 * time) / B
    if  int(z) != 1 :
        #print("Here Z is not equal to 1")
        secondPart = (1 - np.exp(-1 * B * time)) * ((p * (1 - p) * np.power((x1 - x2), 2)) / np.power(B, 2))
    else :
        secondPart = 0
    #print ("rate="+ lambda + "z=" + z);
    assert (firstPart + secondPart) >= 0
    return firstPart + secondPart

#Formula A.5
def MCycle(fId):
        #print ("  %lf  %lf  %lf  %lf   %lf  %lf\n",phit, lambda, x1, x2, firstPart, secondPart);
        rate = landa * zipfs_pop(fId, alpha)
        phit = CumF(rate, CTime)
            #if theoreticalDownTime==0:
            #hin = CumF(rate, CTime)
            #else:
            #hin = CumG(rate, CTime)
        hin = dehghancumG(rate, CTime)
        #if fId==1:
        #print("f:%i\trate=%e\tphit=%e\tpin=%e" %(fId, rate, phit , hin))
        if not isclose(phit, 1):
            #print("Hit is not close to 1")
            return (hin / ( 1- phit))
        else:
            #print("Hit is  close to 1")
            return np.finfo(np.float64).max


#Formula A.6
def expectedMinus2(fId):
    rate = landa * zipfs_pop(fId, alpha)
    p = (z / (z + 1));
    x1 = z * rate;
    x2 = (1.0 / z) * rate;
    B = ((1 - p)/x2 + p/x1);
    
    FTC = CumF(rate, CTime)
    FTCAge = dehghancumG(rate, CTime)
    
    sum = 1
    #if fId<0:
    #print("rate=%e\tFTC=%e" %(rate, FTC))
    
    g = lambda x : 1 - CumF(rate, x) #A.8 integral function
    h1 = lambda x : FTCAge - dehghancumG(rate, x) #A.9 integral functio
    
    if not isclose(FTC, 1.0) and not isclose(FTCAge, 0.0):
        sum =(1-FTCAge)*CTime + FTCAge* ( integrate.quad(h1, 0, CTime, epsabs=0.1, epsrel=0.1)[0]/FTCAge - (B * (1- CumG(rate, CTime)))/(1-FTC) )
    elif isclose(FTC, 1.0) and not isclose(FTCAge, 0.0):
        sum =(1-FTCAge)*CTime + FTCAge* ( integrate.quad(h1, 0, CTime, epsabs=0.1, epsrel=0.1)[0]/FTCAge)
    elif isclose(FTCAge, 0.0):
        sum =CTime
    #if fId<0:
    #print("rate=%e\tsum=%e" %(rate, sum))
    #assert sum>=0
    return sum


def dehghanCachedim():
    result = 0
    for f in range(1,CATALOG+1):
        rate = landa * zipfs_pop(f, alpha)
        #FTC = dehghancumG(rate, CTime)
        #Formula A.4
        cycle = (1+ counting_function(rate, theoreticalDownTime) + MCycle(f))/rate;
        #pin = (MCycle(f)/rate)/cycle;
       
        #pin =( (expectedTD(f) + MCycle(f)/rate-expectedTE(f)))/cycle
        pin =(expectedMinus2(f) + MCycle(f)/rate)/cycle
        assert pin >= 0
        result = result + pin;
        #result = result + (expectedTD(f) + (MCycle2(f))/rate + CTime - expectedTE(f))/cycle;
        #if f%1000==0:
        #   print("f:%i\trate=%e\tpin=%e\tsum=%e" %(f, rate, pin, result))
    return result

#loop for calculating charactersitic time
def computeDehghanAnalyticalCharacteristicTime():
    maxTry = 1000
    nrOfTry = 0
    #CTime = cacheLimit / landa
    global CTime
    CTime = cacheLimit / landa
    #CTime = 0.0001
    C = 0
    while True:
        nrOfTry = nrOfTry +1
        C = dehghanCachedim()
        print("%e\t%e"%(C,CTime))
        CTime = CTime * 2.0;
        if not((C < cacheLimit) and (nrOfTry < maxTry)):
            break
        
    if nrOfTry == 1:
        print("error inital TC too large")
        assert False
        
    if nrOfTry == maxTry:
        print("error initial TC  too small")
        assert False
        
    TC2 = CTime / 2.0
    TC1 = CTime / 4.0
    while True:
        CTime = (TC1 + TC2) / 2.0
        C = dehghanCachedim();
        print("%f\t%f"%(C,CTime))
        if C < cacheLimit :
            TC1 = CTime
        else:
            TC2 = CTime
        #printf("%lf %lf %lf  %lf \n",C,TC, TC1, TC2);
        if not(np.abs(C - cacheLimit) / C > epsilon):
            break
        #printf("c\n");
    #print(CTime);
    return CTime;

#Formula A.3
def computeDehghanAnalyticHitProbability(fId):
    CTime = getCTime()
    rate = landa * zipfs_pop(fId, alpha)
    #print(rate)
    cycle = (1+ counting_function(rate, theoreticalDownTime) + MCycle(fId))
    return  MCycle(fId)/cycle

###################### End Of Method B #############################
###################### Start Of Method A ###########################

#Caclulating moments of R
def meanR(fId):
     rate = landa * zipfs_pop(fId, alpha)
     p = (z / (z + 1));
     x1 = z * rate
     x2 = (1.0 / z) * rate
     B = ((1 - p) * x1 + p * x2)
     C = ((p * (1 - p) * np.power((x1 - x2), 2)) / np.power(B, 2));
     firstPart = (x1 * x2 * np.power(theoreticalDownTime, 2)) / B / 2
     exp = theoreticalDownTime + (np.exp(-1 * B * theoreticalDownTime) - 1) / B
     if z!=1:
        secondPart = exp * C
     else:
        secondPart = 0
     expectedR = (theoreticalDownTime + firstPart + secondPart) / (counting_function(rate, theoreticalDownTime) + 1);
     if  expectedR < 0 :
         assert expectedR >= 0
     return expectedR
    
def varianceR(fId):
    rate = landa * zipfs_pop(fId, alpha)
    p = (z / (z + 1));
    x1 = z * rate
    x2 = (1.0 / z) * rate
    B = ((1 - p) * x1 + p * x2)
    C = ((p * (1 - p) * np.power((x1 - x2), 2)) / np.power(B, 2));
    firstPart = (x1 * x2 * np.power(theoreticalDownTime, 3)) / B / 3;
    exp = np.power(theoreticalDownTime, 2) - (2 * theoreticalDownTime / B) - ((2 * np.exp(-1 * B * theoreticalDownTime) - 2) / np.power(B, 2));
    if z!=1:
        secondPart = exp * C
    else:
        secondPart = 0
    expectedR = (np.power(theoreticalDownTime, 2) + firstPart + secondPart) / (counting_function(rate, theoreticalDownTime) + 1);
    if expectedR < 0:
        assert expectedR >= 0
    varince = expectedR - np.power(meanR(fId), 2);
    return varince


def CV(fId):
   c = 0
   if meanR(fId) != 0 :
       c = (varianceR(fId) / pow(meanR(fId), 2))
   else:
       c = 0
   return c

# Eq. 11
def mahdiehCachedim():
  result = 0;
  for f in range(1, CATALOG+1):
      rate = landa * zipfs_pop(f, alpha)
      #q1 = CumF(rate, CTime + meanR(f)) - CumF(rate, meanR(f))
      #qPrime1 = CumG(rate, CTime + meanR(f)) - CumG(rate, meanR(f))
      qPrime = qPrimeIntegral(f)
      q = qIntegral(f)
      b = CumG(rate, CTime)
      a = CumF(rate, CTime)
      if (1 - a + q) != 0:
          pin = ((q * b + qPrime * (1 - a)) / (1 - a + q))
      else:
          pin = 1
      result += pin;
          #if f<0:
          #print("f:%i\trate=%e\tpin=%e\tsum=%e" %(f, rate, pin, result))

  return result;


#Eq. (15) for \hat Z
def qPrimeIntegral(fId):
    rate = landa * zipfs_pop(fId, alpha)
    k = globalKR[fId-1]
    if k==0 :
        return CumG(rate, CTime + globalMeanR[fId-1]) - CumG(rate, globalMeanR[fId-1])

    rateR = k / globalMeanR[fId-1];
    #rateR = rate;
    #ErlangDist test = new ErlangDist(k, rateR);
    q = lambda x : ((CumG(rate, CTime + x) - CumG(rate, x))* stats.erlang.pdf(x, k, scale = 1.0/rateR))/globalCDF[fId-1]
    sum = integrate.quad(q, 0, theoreticalDownTime, epsabs=0.1, epsrel=0.1)[0]
    return sum


#Eq. (15) for Z
def qIntegral(fId):
    rate = landa * zipfs_pop(fId, alpha)
    k = globalKR[fId-1]
    if k==0 :
        return CumF(rate, CTime + globalMeanR[fId-1]) - CumF(rate, globalMeanR[fId-1])
 
    rateR = k / globalMeanR[fId-1];
    #rateR = rate;
    #ErlangDist test = new ErlangDist(k, rateR);
    q = lambda x : ((CumF(rate, CTime + x) - CumF(rate, x))* stats.erlang.pdf(x, k, scale = 1.0/rateR))/globalCDF[fId-1]
    sum = integrate.quad(q, 0, theoreticalDownTime, epsabs=0.1, epsrel=0.1)[0]
    return sum

#moment matching
def computeMahdiehRandK():
    global globalMeanR
    global globalKR
    global globalCDF
    k = 2
    for f in range(1, CATALOG+1):
        rate = landa * zipfs_pop(f, alpha)
        cv = CV(f)
        if cv < 0.09 :
            k = 0
        else:
            k = round(1/cv)

        globalMeanR[f-1] = meanR(f);
        globalKR[f-1] = k
        if not k == 0:
            globalCDF[f-1] = stats.erlang.cdf(theoreticalDownTime, k, scale = globalMeanR[f-1]/k)

#main loop for calculting CTA with epsilon precision
def computeMahdiehAnalyticalCharacteristicTime():
    maxTry = 1000
    nrOfTry = 0
    #CTime = cacheLimit / landa
    global CTime
    CTime = cacheLimit / landa
    #CTime = 0.000000000001
    C = 0
    while True:
        nrOfTry = nrOfTry +1
        C = mahdiehCachedim()
        print("%e\t%e"%(C,CTime))
        CTime = CTime * 2.0;
        if not((C < cacheLimit) and (nrOfTry < maxTry)):
            break

    if nrOfTry == 1:
        print("error inital TC too large")
        assert False
        
    if nrOfTry == maxTry:
        print("error initial TC  too small")
        assert False

    TC2 = CTime / 2.0
    TC1 = CTime / 4.0
    while True:
        CTime = (TC1 + TC2) / 2.0
        C = mahdiehCachedim()
        print("%f\t%f"%(C,CTime))
        if C < cacheLimit :
            TC1 = CTime
        else:
            TC2 = CTime
        #printf("%lf %lf %lf  %lf \n",C,TC, TC1, TC2);
        if not(np.abs(C - cacheLimit) / C > epsilon):
            break
    #printf("c\n");
    #print(CTime);
    return CTime;

# Eq 12
def computeMahdiehAnalyticHitProbability(fId):
    #print(fId)
    CTime = getCTime();
    rate = landa * zipfs_pop(fId, alpha)
    a = CumF(rate, CTime);
    q = qIntegral(fId);
    if (1 - a + q) != 0:
        phit = q / (1 - a + q)
    else:
        phit = 1
    #double phit = (MCycle(fId))/(MCycle(fId)+1+counting_function(rate,theoreticalDownTime));
    return phit;
###################### End Of Method A ###########################



#calculating the total hit probability
def computeTotalAnalyticHitProbability():
    #if policy == 0:
    #    computeMahdiehRandK()
    mean = 0
    for f in range(1, CATALOG+1):
        #print(zipfs_pop(f, alpha) )
        if policy == 2:
            mean = mean + zipfs_pop(f, alpha) * computeDehghanAnalyticHitProbability(f)
        else:
            mean = mean + zipfs_pop(f, alpha) * computeMahdiehAnalyticHitProbability(f)
    return mean


#default values for the experiment of appendix section
CATALOG = 10**6
landa = 10**5
alpha= 0.8
cacheLimit = 0.001*CATALOG
z= 10.0
policy = 2 #0: Mahdieh 2:Dehghan
sumzipf = 0
CTime = -1
theoreticalDownTime = 0.1
epsilon = 10**-3

#sumzipf = initizipf(alpha, CATALOG)
#z=10
#CTime = 0.3749946594238281
#CTime = 0.4394009590148925
#z=1
#CTime = 0.14724795818328862
#CTime = 0.16381902848000002
#CTimeArray = [2.648437e-03, 1.558594e-02, 1.279297e-01]



#print 'Number of arguments:', len(sys.argv), 'arguments.'
#print 'Argument List:', str(sys.argv[1])
policy = int(sys.argv[1])
globalMeanR = np.zeros(CATALOG)
globalKR = np.zeros(CATALOG)
with open('result'+str(policy)+'.txt', 'w') as saveFileText:
    for d in xrange(6, 8):
        alpha = 0.8
        z = float(sys.argv[2])
        theoreticalDownTime = float(sys.argv[3])
        CTime = -1
        landa = 10**5
        CATALOG = 10**d
        cacheLimit = 0.001*CATALOG
        globalMeanR = np.zeros(CATALOG)
        globalKR = np.zeros(CATALOG)
        globalCDF = np.zeros(CATALOG)
        sumzipf = initizipf(alpha, CATALOG)
      
        print("Policy:%d\tK:%e\tC=%e\tz=%f\talpha=%f\tD=%f\tlanda=%e" %(policy, CATALOG, cacheLimit, z, alpha, theoreticalDownTime, landa))
        start = time.clock()
        if policy == 0:
                  computeMahdiehRandK()
        phit = computeTotalAnalyticHitProbability()
        end  = time.clock()
        if policy==2:
            cacheSize = dehghanCachedim()
            #cacheSize = 0
            saveFileText.write("%e\t%e\t%e\t%e\t%e\n" %(CATALOG, phit, (end-start), CTime,cacheSize))
            print("%e\t%e\t%e\t%e\t%e\n" %(CATALOG, phit, (end-start), CTime,cacheSize))
        else:
            cacheSize =  mahdiehCachedim()
            saveFileText.write("%e\t%e\t%e\t%e\t%e\n" %(CATALOG, phit, (end-start), CTime, cacheSize))
            print("%e\t%e\t%e\t%e\t%e\n" %(CATALOG, phit, (end-start), CTime, cacheSize))







