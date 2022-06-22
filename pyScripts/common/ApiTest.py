import java
import polyglot

print('Hello from ApiTest.py')

MyTrait2 = java.type('pytest.MyTrait2')

class MyClass2(MyTrait2):
    def foo(self, i: int) -> str:
        print(f"Called foo({i})")
        return f"Foo {i}!"

MyClass2()
