# feedzai Open Scoring Server (FOS) - Weka Scoring and Training module

fos-weka is a [fos-core] supporting [Weka] classifiers


[![Build Status](https://feedzaios.ci.cloudbees.com/buildStatus/icon?job=fos-weka)](https://feedzaios.ci.cloudbees.com/job/fos-weka/)

[![CloudbeesDevCloud](http://www.cloudbees.com/sites/default/files/Button-Built-on-CB-1.png)](http://www.cloudbees.com/dev)



## Why FOS

There are pretty good machine learning training and scoring frameworks/libraries out there, but they don't provide the
following benefits:

1. Common API: fos provides a common abstraction for model attributes, model training and model scoring. Using a [Weka]
based classifier will use have exactly the same API as using a R based classifier.
1. Scoring & Training as a remote service: Training and scoring can be farmed to dedicated servers in the network
enabling both vertical and horizontal scaling.
1. Import and Export models: A model could be trained in a development box and imported seamlessly into a remote server
1. Scalable and low latency scoring: Marshalling and Unmarshalling scoring requests/responses can be responsible
for a significant amount of overhead. Along with the slow RMI based interface, fos also supports scoring using [Kryo].

## Compiling fos-weka

You need:

1. [Java SDK]: Java 7
1. [Maven]: Tested with maven 3.0.X
1. [fos-core]
1. Access to maven central repo (or a local proxy)

After both the [Java SDK] and [Maven] are installed run the following command

`mvn clean install`

This should compile fos-core, ran all the tests and install all modules into your local maven repo.


## Running FOS


## FAQ

### Weka is great but i need to support another library. How can I do it?
fos-core does not provide any concrete implementation. You may want to peek at fos-impl-dummy module in [fos-core].
After you've familiarized with the API (Manager, ManagerFactory and Scorer) you can take a peek at a real implementation,
say [fos-weka].

[Kryo]: https://github.com/EsotericSoftware/kryo
[fos-r]: https://github.com/feedzai/fos-r
[fos-core]: https://github.com/feedzai/fos-core
[Weka]: http://www.cs.waikato.ac.nz/ml/weka/
[R]: http://www.r-project.org/
[Maven]: http://maven.apache.org/
[Java SDK]: http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html



