# Javadoc

## Build Javadoc

1. change to the root dir of your git clone
2. build javadoc for all modules in html format in subdirectory javadoc

```mvn javadoc:aggregate -P generate_developer_docs -Ddoctarget=$(pwd) -Ddocdir=javadoc '-P!development'```

## Read Javadoc

Open `javadoc/index.html`
