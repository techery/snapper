## Snapper
NoSQL fast-serializable storage with Projections, Sorting, Filtering and more.

## Overview
NoSQL db is great in simplicity.
`Snapper` is simple but yet functional:
it's based on idea that every `POJO` could be addressed as typed `DataCollection` with
any kind of `Projection` (aka sub-set) for filtering, sorting, mapping or joining.
Such `DataCollection`\\`Projection` is observable and reacts to changes from parent `DataSet`.

## Performance
Timing is very close to fastest `SQLite`-based DBs.
See [Benchmark test results](BENCHMARK.md) for details.

## Getting started
### Core objects
`DroidSnapper` is main controller for `DataCollection`(s) - data provider objects.
It's thread safe Singleton:

```java
DataCollection<User> userCollection = DroidSnapper.with(context).collection(User.class);
```
`DataCollection` itself is used for:
- data manipulation

   ```java
   userCollection.insert(new User(1, "Jim")); // add user with id=1
   userCollection.insert(new User(1, "Jim Defoe")); // replace user by id 
   ```
   _Note_: all data-related work is done on background `Executor` so `MainThread`-friendly.
- data observation

  ```java
  userCollection.addDataListener(new IDataSet.DataListener<User>() {
      @Override
      public void onDataUpdated(List<User> items, StorageChange<User> change) {
          //
      }
  });
  ```
  _Note_: `DataCollection` holds memory cache from it's storage for better performance;
  
  _Note_: listener is called right away upon addition, it's guaranteed' that `DataCollection` is initialized first.
  
  _Caution_: listener is called from `Executor`'s thread, 
  it's up to `DataListener` to proxy calls to another thread, e.g. [MainThreadDataListener](library/src/main/java/io/techery/snapper/util/android/MainThreadDataListener.java).
- `Projection` creation:

  ```java
  Projection<User> jimProjection = userCollection.projection()
          .where(new Predicate<User>() {
              @Override
              public boolean apply(User element) {
                  return element.name.contains("Jim");
              }
          }).build();
  jimProjection.addDataListener(...);
  ```
`Projection` is a subset of parent `DataSet` which `DataCollection` or another `Projection` is.
It's used for:
    - sub-projection with condition;
    - data observation.

### Data Model
Every model must implement `Indexable` interface, e.g.:
```java
public class User implements Indexable {

    public final int id;
    
    public User(int id) {
        this.id = id;
    }

    @Override
    public byte[] index() {
        return ByteBuffer.allocate(4).putInt(id).array();
    }
}
```
- `index()` is used as unique key for each object;
- insertion will replace (update) object with same index.
  
## Advanced usage
### DataCollection naming
Every POJO storage is name'd with POJO's `class#getSimpleName()`, but could be labeled for uniqueness:
```java
DataCollection<User> userCollection = DroidSnapper.with(context).collection(User.class);
DataCollection<User> friendsCollection = DroidSnapper.with(context).collection(User.class, "friends");
```
### Advanced DataSet(s)
#### Joining
Connects two `DataSet`s to listen for their changes:
```java
DataSetJoin<Company, User, Pair<Company, User>> companyUserJoin =
    new JoinBuilder<>(companyStorage, userStorage)
            .setJoinFunction(new Function2<Company, User, Boolean>() {
                @Override public Boolean apply(Company company, User user) {
                    return company.getId() == user.getCompanyId();
                }
            })
            .setMapFunction(new Function2<Company, List<User>, Pair<Company, User>>() {
                @Override public Pair<Company, User> apply(Company company, List<User> users) {
                    User user = users.isEmpty() ? null : users.get(0);
                    return new Pair<>(company, user);
                }
            }).create();
```
#### Mapping
Creates virtual relation between `DataSet` objects and some type:
```java
DataSetMap<User, Integer> userIdMap = new DataSetMap<User, Integer>(dataCollection, new Function1<User, Integer>() {
    @Override
    public Integer apply(User user) {
        return user.id;
    }
});
userIdMap.addDataListener(new IDataSet.DataListener<Integer>() {
    @Override
    public void onDataUpdated(List<Integer> items, StorageChange<Integer> change) {
        //
    }
});
```
### Resources Cleanup
- Every collection could be closed with `DataCollection#close()`
- All collections are closed via `Snapper#close()`

## Under the hood
`DroidSnapper` is a `Snapper` instance with default impl. of `Snapper`'s components, those are:
- `StorageFactory` defines rules for creating a storage per POJO's model;
- `ComponentFactory` provides main mechanisms:
 - `DatabaseAdapter` – links model's storage with real persister;
 - `ObjectConverter` - converts model to bytes and vice-versa to be stored by model's storage; 
 - `StorageExecutor` – `Executor` to do job for storage manipulation;
 - `CollectionExecutor` – `Executor` to do job for `DataCollection` actions;

It means one can easily create own no-sql storage with own components of choice.

By default, `DroidSnapper` uses
- [SnappyDB](https://github.com/nhachicha/SnappyDB) as `DatabaseAdapter`
- [Kryo](https://github.com/EsotericSoftware/kryo) as `ObjectConverter`

## Dev. status
Tested in production.

[![Build Status](https://travis-ci.org/techery/snapper.svg?branch=master)](https://travis-ci.org/techery/snapper)
![JitPack release](https://img.shields.io/github/tag/techery/snapper.svg?label=JitPack)

## Installation
```groovy
repositories {
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.techery:snapper.library:{latestVersion}'
}
```

## License

    Copyright (c) 2015 Techery

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

