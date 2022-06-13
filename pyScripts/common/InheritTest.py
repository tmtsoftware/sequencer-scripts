
import java
import polyglot

print('Hello from InheritTest.py')

# MyTrait = java.type('pytest.MyTrait')

class MyClass:
    def foo(self, i: int) -> str:
        print(f"Called foo({i})")
        return f"Foo {i}!"

polyglot.export_value("MyClass", MyClass)
