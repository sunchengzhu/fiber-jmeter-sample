# fiber-jmeter-sample
Stress test the rpc interface of the fiber chain by implementing `java request`

## require
java8,maven,linux or mac(not support win)


1. mod stress jmx

```
  mvn package
  mvn jmeter:gui
  load jmx file
  src/test/jmeter/FiberSendDemo.jmx
```  
2. stress
```
mvn jmeter:jmeter
mvn jmeter:jmeter@configuration2 -DjmeterTest=FiberSendDemo.jmx
```

