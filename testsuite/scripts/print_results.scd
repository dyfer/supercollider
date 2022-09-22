var makeRed, makeYellow, makeGreen, makeBold, print, allStr = "";
var useColors = false, exitWhenDone = false;
var passed = 0, failed = 0, skipped = 0;
var results, path, tmpPath, file;

if(thisProcess.argv.size > 0) {
	path = thisProcess.argv[0];
	useColors = true; // we assume that when a command line argument is passed, we run in a terminal
	exitWhenDone = true;
} {
	path = PathName(thisProcess.nowExecutingPath).pathOnly +/+ "run" +/+ "test_result.scxtar";
};

results = Object.readArchive(path);

makeRed = {|str|
	if(useColors) {
		str = "\\x1b[31m%\\x1b[0m".format(str);
	};
	str;
};
makeYellow = {|str|
	if(useColors) {
		str = "\\x1b[33m%\\x1b[0m".format(str);
	};
	str;
};
makeGreen = {|str|
	if(useColors) {
		str = "\\x1b[32m%\\x1b[0m".format(str);
	};
	str;
};
makeBold = {|str|
	if(useColors) {
		str = "\\x1b[1m%\\x1b[0m".format(str);
	};
	str;
};
print = {|str|
	if(useColors) {
		// "echo \"%\"".format(str.escapeChar($")).systemCmd
		// "echo \"%\"".format(str.escapeChar($")).unixCmd;
		// ["echo", str].unixCmd;
		allStr = allStr ++ str ++ "\n";
	} {
		str.postln;
	};
};

results.do({|dict|
	var allResults, passedResults, header, name;
	name = makeBold.("%:%".format(dict[\suite], dict[\test]));
	if(dict[\skip].asBoolean) {
		// "skipped: %".format(dict[\skipReason]).postln;
		header = makeYellow.("[-]");
		print.("    %   % Skipped (reason: %)".format(header, name, dict[\skipReason]));
		skipped = skipped + 1;
	} {
		allResults = dict[\results].size;
		passedResults = dict[\results].select({|resDict| resDict[\pass] == true}).size;
		// [12/12] TestSymbol:test_isIdentifier (0.0s)
		header = "[%/%]".format(passedResults, allResults);
		if(allResults > 0) {
			if(passedResults == allResults) {
				header = makeGreen.(header);
			} {
				header = makeRed.(header);
			};
			print.("    % % (%s)".format(header, name, (dict[\duration] ? 0).round(0.01)));
			dict[\results].do({|resDict|
				if(resDict[\pass]) {
					header = makeGreen.("*");
					passed = passed + 1;
				} {
					header = makeRed.("!");
					failed = failed + 1;
				};
				print.("           % %".format(header, resDict[\test].asString));
			})
		} {
			print.("    %   % (%s)".format(makeRed.("[!]"), name, (dict[\duration] ? 0).round(0.01)));
			failed = failed + 1;
		}
	};
});

if(failed > 0) {
	print.("    %, %, out of %".format(makeRed.("% TESTS FAILED".format(failed)), makeYellow.("% skipped".format(skipped)), passed + skipped + failed));
	print.("Search for ! to find failed tests");
} {
	print.("    %, %".format(makeGreen.("% TESTS PASSED".format(passed)), makeYellow.("% skipped".format(skipped))));
};

if(useColors) {
	try {
		tmpPath = PathName.tmp +/+ 10000.rand.asString ++ "testStr.txt";
		file = File(tmpPath, "w");
		file.putString(allStr);
		file.close;
		// ~file.close;
		// "cat %".format(tmpPath.shellQuote).unixCmd;
		"echo -ne \"$(</%)\"".format(tmpPath.shellQuote).unixCmd({
			File.delete(tmpPath);
		});
	} { |err|
		("    % TESTS PASSED, % TESTS FAILED, % skipped, out of %".format(passed, failed, skipped, passed + skipped + failed)).postln; // provide rudimentary posting in case something goes wrong with the posting
	};
};

if(exitWhenDone) {failed.exit};

