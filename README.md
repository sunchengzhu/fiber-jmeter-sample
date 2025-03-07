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
rm -rf target/jmeter/ || rm -f jmeter.tar.gz || rm -f /tmp/scz/jmeter.tar.gz
nohup mvn jmeter:jmeter@configuration2 -DjmeterTest=FiberSendDemo.jmx > /dev/null 2>&1 &

tar -czf jmeter.tar.gz target/jmeter/
mv jmeter.tar.gz /tmp/scz/
```

