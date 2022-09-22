//add unit tests

var path = thisProcess.argv[0];

path = (path ? "").withoutTrailingSlash;
if(File.exists(path), {
	postf("adding include path %\n", path);
	LanguageConfig.addIncludePath(path);
	LanguageConfig.store;
}, {
	format("Specified path doesn't exist, not adding: %", path).warn;
});

0.exit;
