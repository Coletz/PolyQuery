# PolyQuery
An attempt to "bypass" the limitation about polymorphism in Realm. Performance are not good. This is an early alpha PoC


# Usage
Annotate your interface with @PolyQuery and build the project.
The annotation processor will generate <YourInterface>PolyQuery.java file.
PS: The annotation MUST extend RealmModel.
PPS: You can query only fields that are on EVERY class that implements your annotation

The generated file will include equalTo and notEqualTo methods, that one can use to query a group of classes.
In a future release the goal is to have every function that a RealmQuery provides.

```Java
interface MyInterface extends RealmModel { .. }

@PolyQuery(MyInterface.class)
class MyFirstClass extends MyInterface { .. }
@PolyQuery(MyInterface.class)
class MySecondClass extends MyInterface { .. }

..

// build the project, MyInterfacePolyQuery.java will be generated


Realm realm = Realm.getDefaultRealm();
MyInterface result = new MyInterfacePolyQuery(realm)
        .equalTo("primaryKey", "a nice String key")
        .queryFirst();
// result is a managed object, already casted to MyInterface

// if needed, you can get if it's a MyFirstClass object or MySecondClass object with instanceof
if (result instanceof MyFirstClass) {
  // do things
} else if (result instanceof MySecondClass) {
  // do other things
}

```

# Drawbacks
 - Async query are not supported
 - Results from the `query` method are RealmList and not RealmResults. In a future release I'll add at least something to return a List<RealmResults<T>> to manage multiple async results
