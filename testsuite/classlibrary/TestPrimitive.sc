TestPrimitive_Obj {
	noKwargsNoVarArgs { |a, b, c|
		_PrimitiveTestNoKwargsNoVarArg;
		^this.primitiveFailed
	}

	noKwargsWithVarArgs { |a, b, c... args|
		_PrimitiveTestNoKwargsWithVarArgs;
		^this.primitiveFailed
	}

	kwargsWithVarArgs { |a, b, c... args, kwargs|
		_PrimitiveTestWithKwargs;
		^this.primitiveFailed
	}
}

TestPrimitives : UnitTest {
	// basic
	test_no_kw_no_var_args {
		var obj = TestPrimitive_Obj();
		this.assertEquals(
			obj.noKwargsNoVarArgs(1, 2, 3),
			[obj, 1, 2, 3]
		);
		this.assertEquals(
			obj.noKwargsNoVarArgs(a: 1, b: 2, c: 3),
			[obj, 1, 2, 3]
		);
		this.assertEquals(
			obj.noKwargsNoVarArgs(1, 2, c: 3),
			[obj, 1, 2, 3]
		);
		this.assertEquals(
			obj.noKwargsNoVarArgs(1, 2, 3, 4, 5),
			[obj, 1, 2, 3]
		);
		this.assertEquals(
			obj.noKwargsNoVarArgs(1, 2, 3, 4, f: 5),
			[obj, 1, 2, 3]
		);
	}

	// basic, with var args
	test_no_kw_with_var_args {
		var obj = TestPrimitive_Obj();
		this.assertEquals(
			obj.noKwargsWithVarArgs(1, 2, 3),
			[obj, 1, 2, 3]
		);
		this.assertEquals(
			obj.noKwargsWithVarArgs(a: 1, b: 2, c: 3),
			[obj, 1, 2, 3]
		);
		this.assertEquals(
			obj.noKwargsWithVarArgs(1, 2, c: 3),
			[obj, 1, 2, 3]
		);
		this.assertEquals(
			obj.noKwargsWithVarArgs(1, 2, 3, 4, 5),
			[obj, 1, 2, 3, 4, 5]
		);
		this.assertEquals(
			obj.noKwargsWithVarArgs(1, 2, 3, 4, f: 5),
			[obj, 1, 2, 3, 4]
		);
	}

	test_kw_with_var_args {
		var obj = TestPrimitive_Obj();
		this.assertEquals(
			obj.kwargsWithVarArgs(1, 2, 3),
			[obj, 1, 2, 3]
		);
		this.assertEquals(
			obj.kwargsWithVarArgs(a: 1, b: 2, c: 3),
			[obj, 1, 2, 3]
		);
		this.assertEquals(
			obj.kwargsWithVarArgs(1, 2, c: 3),
			[obj, 1, 2, 3]
		);
		this.assertEquals(
			obj.kwargsWithVarArgs(1, 2, 3, 4, 5),
			[obj, 1, 2, 3, 4, 5]
		);
		this.assertEquals(
			obj.kwargsWithVarArgs(1, 2, 3, 4, f: 5),
			[obj, 1, 2, 3, 4, f: 5]
		);
	}
}
