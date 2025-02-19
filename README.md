# repl-kit

A minimal clojure editor with REPL integration

## Motivation
This is not a replacment for great REPL editor tools such as cider and calva. Frequently I need to work in restricted environments where I cannot install those tools. I also need something that is self-contained and published to maven central, again due to restricted environments.

## TODO
- Set current namespace automatically after loading entire file
  Currently, you can simply execute the ns form of the file.
- Add dirty state of file indicator? 



## Publishing to Maven Central (Note to Self)
```bash
clojure -T:build uberjar
mvn package
mvn deploy
```

