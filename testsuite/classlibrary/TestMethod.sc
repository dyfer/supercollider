/*
TestMethod.run
UnitTest.gui
*/

TestMethod : UnitTest {

	test_method_argumentString {
		var methodsThatFailed = List.new;
		Object.allSubclasses.do({ arg item;
			item.methods.do({ arg jtem;
				var method = jtem;
				try {
					method.argumentString;
				} {|err|
					".argumentString FAILED for: %".format(method.asString).postln;
					methodsThatFailed.add(method);
				}
			});
		});

		this.assert(
			methodsThatFailed.size == 0,
			"The method argumentString failed for % methods".format(methodsThatFailed.size)
		);
	}

	test_classnameAsMethodCrash {

		this.assertException({ 1 fooBar: 2; }, DoesNotUnderstandError, "1 fooBar: 2 threw an exception (no method)", true);

		this.assertException({ 1 FooBar: 2; }, DoesNotUnderstandError, "1 FooBar: 2 threw an exception (FooBar is not a method)", true);

		this.assertException({ 1 Object: 2; }, DoesNotUnderstandError, "1 Object: 2 threw an exception (Object is not a method)", true);
	}

} // End class
