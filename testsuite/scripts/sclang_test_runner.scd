// File.delete("~/Desktop/tests.json".standardizePath);
// File.copy("../../../supercollider/testsuite/scripts/gha_test_run_proto.json".resolveRelative, "~/Desktop/tests.json".standardizePath);

// this script is meant to be run with two arguments: path to test prototype indicating tests to be run, and path to test result
(
if(thisProcess.argv.size > 0) {
	~testProto = thisProcess.argv[0];
	~testResult = thisProcess.argv[1];
} {
	~testProto = PathName(thisProcess.nowExecutingPath).pathOnly +/+ "gha_test_run_proto_gha.scd";
	// ~testProto = PathName(thisProcess.nowExecutingPath).pathOnly +/+ "gha_test_run_proto_all.scd"; // for testing
	~testResult = PathName(thisProcess.nowExecutingPath).pathOnly +/+ "run" +/+ "test_result.scxtar";
};

if(File.exists(~testProto).not) {
	Error("test prototype file doesn't exist").throw
};
// ~testRecord = "~/Desktop/tests.json".standardizePath;

~writeResult = {|results, path|
	// var resultsString = JSON.stringify(results);
	// var file = File(path, "w");
	// var success = false;
	// file.putString(resultsString);
	results.writeArchive(path);
	// file.close();
};

// safely extract the test record dictionary from the input file
~getTestRecord = {|file|
	var test_record, test_record_string;
	var success = false;

	test_record_string = File(file, "r").readAllString();

	test_record = test_record_string.interpret();

	test_record;
};


// expand { suite: * } to a dictionary of { test: * } records
~expandSuiteGlob = { |testsDict|
	var suiteGlob = testsDict.select { |item| item[\suite] == "*" };

	if (suiteGlob.notEmpty) {
		var all = UnitTest.allTestClasses.keys.select({ |t| t != "...All..." }).asArray.collect({ |className|
			Dictionary.newFrom((\suite: "Test" ++ className, \test: "*"))
		});
		testsDict.removeAll(suiteGlob);
		testsDict = all ++ testsDict;
	};

	testsDict;
};

~testrun = {|protoFile, resultFile|
	var tests = ~getTestRecord.(protoFile);
	var skipped, toExpand, toExclude;

	// if (test_record["tests"].isEmpty) {
	// Error("testrun: No tests defined").throw;
// };

	// try {

		UnitTest.findTestClasses();

		tests = ~expandSuiteGlob.(tests);

		tests = List.newFrom(tests.collect(Dictionary.newFrom(_)));

		// tests = test_record["tests"];
		// settings = test_record["settings"];
		// ~tests = tests;

		// Expand *'s
		toExpand = tests.select({|t| (t[\test] == "*") && (t[\completed] != true) });
		"Expanding %\n".postf(toExpand);
		toExpand.do {|wildcardTest|
			var allTests, newTest, class;

			class = wildcardTest[\suite].asSymbol.asClass;

			if (class.respondsTo(\findTestMethods).not && class.notNil) {
				class = ("Test" ++ class.name.asString).asSymbol.asClass;
			};

			if (class.isNil) {
				wildcardTest[\error] = "Class % not found".format(class);
				wildcardTest[\completed] = true;
				// ~writeResult.(test_record, file);
			} {
				"class: %".format(class).postln;
				class.tryPerform(\findTestMethods).do {|testMethod|
					"method: %".format(testMethod.name).postln;
					newTest = Dictionary.newFrom((
						\suite: class.name.asString,
						\test: testMethod.name,
						\skip: wildcardTest[\skip],
						\skipReason: wildcardTest[\skipReason],
					));

					tests.add(newTest);
					"tests.size: %".format(tests.size).postln;
				};
				tests.remove(wildcardTest);
				// ~writeResult.(test_record, file);
			}
		};

	// ~tests = tests;

		1.wait; // it seems we need time to finish wring to the file...?
		// Ensure excluded tests are not run
		toExclude = tests.select({|t|
			if (t[\skip].notNil) {
				t[\skip] = (t[\skip].asString.toLower == "true")
			};

			t[\skip] == true
		});

		"Excluding: %".format(toExclude.join(", ")).postln;
		tests = tests.reject({|t|
			(toExclude.detect({|excluded|
				(t[\suite].asString == excluded[\suite].asString)
				&& (t[\test].asString == excluded[\test].asString)
				&& (t !== excluded)
			}).size > 0)
		});
		// test_record["tests"] = tests;
		// ~writeResult.(tests, file);
		"After exclude: ".post;
		tests.do(_.postln);



		1.wait; // it seems we need time to finish wring to the file...?
		// Okay, time to run the tests
		tests.do {|test|
			var class, testname, script, result,
			oldFailures, oldPasses, newPasses, newFailures,
			startTime, endTime,
			success = false;
			"\n\tTest: %\n".postf(test[\test]);

			try {
				if (test[\completed].isNil) {
					if (test[\skip] == true) {
						test[\completed] = true;
						test[\attemptsRemaining] = nil;
						~writeResult.(tests, resultFile);
					} {
						test[\completed] = false;
						class = test[\suite].asSymbol.asClass;
						testname = test[\test].asSymbol;
						if (class.isNil) {
							test[\error] = "Class % not found".format(class);
							test[\completed] = true;
							~writeResult.(tests, resultFile);
						} {
							class.findTestMethods();
							class.setUpClass();

							script = class.findTestMethods().detect({ |m| m.name == testname });
							if (script.notNil) {
								// This is a bad way to get the results, but there's no other for now.
								// One this is working end-to-end, then UnitTest can be improved incrementally.
								oldPasses = IdentitySet.newFrom(class.passes);
								oldFailures = IdentitySet.newFrom(class.failures);

								// RUN THE TEST ///////////////
								~writeResult.(tests, resultFile);
								startTime = Date.localtime();

								result = class.new.runTestMethod(script);
								endTime = Date.localtime();
								test[\completed] = true;
								test[\attemptsRemaining] = nil;
								test[\duration] = endTime.rawSeconds - startTime.rawSeconds;
								///////////////////////////////

								newPasses = IdentitySet.newFrom(class.passes).difference(oldPasses);
								newFailures = IdentitySet.newFrom(class.failures).difference(oldFailures);
								test[\results] = List();
								newPasses.do {|pass|
									test[\results].add((
										\test: ("" ++ pass.message)[0..1000],
										\pass: true,
									))
								};
								newFailures.do {|fail|
									test[\results].add((
										\pass: false,
										\test: ("" ++ fail.message.split($\n)[0])[0..1000],
										\reason: (fail.message.split($\n)[1..])[0..1000]
									))
								};

								~writeResult.(tests, resultFile);
							} {
								test[\error] = "Test not found.";
								test[\completed] = true;
								~writeResult.(tests, resultFile);
							};

							class.tearDownClass();
						}
					}
				};
			} {	|err|
				err.reportError;
				test[\error] = err.errorString;
				~writeResult.(tests, resultFile);
				// success = true;
				/*			};
				if(success.not) {
				// test["error"] = err.errorString; // since we are not catching the error, we can't report it...
				~writeResult.(tests, resultFile);*/
			}
		}
	// };
// } {
// 	|err|
// 	err.reportError;
// }
};


{
	var success = false;
	try {
		~testrun.(~testProto, ~testResult);
	// } { |err|
	// 	err.what.error;
		success = true;
	};
	if(success.not) {
		"~testrun.(~testRecord) failed".error;
	};
	"******** DONE ********".postln;
	// 0.exit;
	if(thisProcess.argv.size > 0) {
		0.exit;
	}; // exit only if the script was called with arguments
}.forkIfNeeded(AppClock);
)
