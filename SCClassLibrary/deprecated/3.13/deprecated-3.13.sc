+ ControlSpec {
	grid {
		this.deprecated(thisMethod, ControlSpec.findMethod(\asGridLines));
		^this.asGridLines;
	}

	grid_ {
		^this.deprecated(thisMethod, nil);
	}
}

+ Nil {
	asGrid { |address|
		this.deprecated(thisMethod, Nil.findMethod(\asGridLines));
		^this.asGridLines;
	}
}