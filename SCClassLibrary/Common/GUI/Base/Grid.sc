

DrawGrid {

	var <bounds,<>x,<>y;
	var <>opacity=0.7,<>smoothing=false,<>linePattern;

	*new { |bounds,horzGrid,vertGrid|
		^super.new.init(bounds, horzGrid, vertGrid)
	}
	*test { arg horzGrid,vertGrid,bounds;
		var w,grid;
		bounds = bounds ?? {Rect(0,0,500,400)};
		grid = DrawGrid(bounds,horzGrid,vertGrid);
		w = Window("Grid",bounds).front;
		UserView(w,bounds ?? {w.bounds.moveTo(0,0)})
			.resize_(5)
			.drawFunc_({ arg v;
				grid.bounds = v.bounds;
				grid.draw
			})
			.background_(Color.white)
		^grid
	}

	init { arg bounds,h,v;
		var w;
		x = DrawGridX(h);
		y = DrawGridY(v);
		this.bounds = bounds;
		this.font = Font( Font.defaultSansFace, 9 );
		this.fontColor = Color.grey(0.3);
		this.gridColors = [Color.grey(0.7),Color.grey(0.7)];
	}
	bounds_ { arg b;
		bounds = b;
		x.bounds = b;
		y.bounds = b;
	}
	draw {
		Pen.push;
			Pen.alpha = opacity;
			Pen.smoothing = smoothing;
			if(linePattern.notNil) {Pen.lineDash_(linePattern)};
			x.commands.do({ arg cmd; Pen.perform(cmd) });
			y.commands.do({ arg cmd; Pen.perform(cmd) });
		Pen.pop;
	}
	font_ { arg f;
		x.font = f;
		y.font = f;
	}
	fontColor_ { arg c;
		x.fontColor = c;
		y.fontColor = c;
	}
	gridColors_ { arg colors;
		x.gridColor = colors[0];
		y.gridColor = colors[1];
	}
	horzGrid_ { arg g;
		x.grid = g;
	}
	vertGrid_ { arg g;
		y.grid = g;
	}
	copy {
		^DrawGrid(bounds,x.grid,y.grid).x_(x.copy).y_(y.copy).opacity_(opacity).smoothing_(smoothing).linePattern_(linePattern)
	}
	clearCache {
		x.clearCache;
		y.clearCache;
	}
}


DrawGridX {

	var <grid,<>range,<>bounds;
	var <>font,<>fontColor,<>gridColor,<>labelOffset;
	var commands,cacheKey;

	*new { arg grid;
		^super.newCopyArgs(grid.asGrid).init
	}

	init {
		range = [grid.spec.minval, grid.spec.maxval];
		labelOffset = 4 @ -10;
	}
	grid_ { arg g;
		grid = g.asGrid;
		range = [grid.spec.minval, grid.spec.maxval];
		this.clearCache;
	}
	setZoom { arg min,max;
		range = [min,max];
	}
	commands {
		var p;
		if(cacheKey != [range,bounds],{ commands = nil });
		^commands ?? {
			cacheKey = [range,bounds];
			commands = [];
			p = grid.getParams(range[0],range[1],bounds.left,bounds.right);
			p['lines'].do { arg val;
				// value, [color]
				var x;
				val = val.asArray;
				x = grid.spec.unmap(val[0]).linlin(0, 1, bounds.left, bounds.right);
				commands = commands.add( ['strokeColor_',val[1] ? gridColor] );
				commands = commands.add( ['line', Point( x, bounds.top), Point(x,bounds.bottom) ] );
				commands = commands.add( ['stroke' ] );
			};
			if(bounds.width >= 12	,{
				commands = commands.add(['font_',font ] );
				commands = commands.add(['color_',fontColor ] );
				p['labels'].do { arg val;
					var x;
					// value, label, [color, font]
					if(val[2].notNil,{
						commands = commands.add( ['color_',val[2] ] );
					});
					if(val[3].notNil,{
						commands = commands.add( ['font_',val[3] ] );
					});
					x = grid.spec.unmap(val[0]).linlin(0, 1 ,bounds.left, bounds.right);
					commands = commands.add( ['stringAtPoint', val[1].asString, Point(x, bounds.bottom) + labelOffset ] );
				}
			});
			commands
		}
	}
	clearCache { cacheKey = nil; }
	copy { ^super.copy.clearCache }
}


DrawGridY : DrawGridX {

	init {
		range = [grid.spec.minval, grid.spec.maxval];
		labelOffset = 4 @ 4;
	}
	commands {
		var p;
		if(cacheKey != [range,bounds],{ commands = nil });
		^commands ?? {
			commands = [];

			p = grid.getParams(range[0],range[1],bounds.top,bounds.bottom);
			p['lines'].do { arg val;
				// value, [color]
				var y;
				val = val.asArray;
				y = grid.spec.unmap(val[0]).linlin(0, 1 ,bounds.bottom, bounds.top);
				commands = commands.add( ['strokeColor_',val[1] ? gridColor] );
				commands = commands.add( ['line', Point( bounds.left,y), Point(bounds.right,y) ] );
				commands = commands.add( ['stroke' ] );
			};
			if(bounds.height >= 20	,{
				commands = commands.add(['font_',font ] );
				commands = commands.add(['color_',fontColor ] );
				p['labels'].do { arg val,i;
					var y;
					y = grid.spec.unmap(val[0]).linlin(0, 1 ,bounds.bottom, bounds.top);
					if(val[2].notNil,{
						commands = commands.add( ['color_',val[2] ] );
					});
					if(val[3].notNil,{
						commands = commands.add( ['font_',val[3] ] );
					});
					commands = commands.add( ['stringAtPoint', val[1].asString, Point(bounds.left, y) + labelOffset ] );
				}
			});
			commands
		}
	}
}


// DrawGridRadial : DrawGridX {}


GridLines {

	var <>spec;

	classvar <>getParamsFunc;


	*new { arg spec;
		^super.newCopyArgs(spec.asSpec)
	}

	asGrid { ^this }
	niceNum { arg val,round;
		// http://books.google.de/books?id=fvA7zLEFWZgC&pg=PA61&lpg=PA61
		var exp,f,nf,rf;
		exp = floor(log10(val.abs));
		f = val.abs / 10.pow(exp);
		if(val < 0) {
			rf = 10.pow(exp).neg;
		} {
			rf = 10.pow(exp);
		};
		if(round,{
			if(f < 1.5,{
				^rf *  1.0
			});
			if(f < 3.0,{
				^rf *  2.0
			});
			if( f < 7.0,{
				^rf *  5.0
			});
			^rf *  10.0
		},{
			if(f <= 1.0,{
				^rf *  1.0;
			});
			if(f <= 2,{
				^rf *  2.0
			});
			if(f <= 5,{
				^rf *  5.0;
			});
			^rf *  10.0
		});
	}
	niceNumWarp { arg val,warp=0;
		var exp,f,nf,rf,negative,rounding,rv;
		exp = floor(log10(val.abs));
		f = val.abs / 10.pow(exp);
		"f: ".post; f.postln;
		"warp: ".post; warp.postln;
		negative = val < 0;
		if(negative) {
			rf = 10.pow(exp).neg;

		} {
			rf = 10.pow(exp);
		};
		if(f<5) {
			warp = (warp.linlin(-8, 0, 0, 1).postln * f.linlin(0, 5, 1, 0).postln).postln.linlin(0, 1, 0, 8);
		} {
			warp = (warp.linlin(0, 8, 0, 1).postln * f.linlin(5, 10, 0, 1).postln).postln.linlin(0, 1, 0, 8);
		};

		"warp after: ".post; warp.postln;
		rounding = (2**warp.round(1)).reciprocal;
		"rounding: ".post; rounding.postln;
		rv = f.roundUp(rounding);
		"rv: ".post; rv.postln;
		"rf: ".post; rf.postln;
		if(f <= rv,{
			^rf *  rv;
		});
		^rf *  10.0
	}
	ideals { arg min,max,ntick=5;
		var nfrac,d,graphmin,graphmax,range,x;
		range = this.niceNum(max - min,false);
		d = this.niceNum(range / (ntick - 1),true);
		graphmin = floor(min / d) * d;
		graphmax = ceil(max / d) * d;
		nfrac = max( floor(log10(d)).neg, 0 );
		^[graphmin,graphmax,nfrac,d];
	}
	looseRange { arg min,max,ntick=5;
		^this.ideals(min,max).at( [ 0,1] )
	}
	getParams { |valueMin,valueMax,pixelMin,pixelMax,numTicks,avgPixDistance|
		// ^getParamsFunc.(valueMin,valueMax,pixelMin,pixelMax,numTicks,avgPixDistance,this)
		var lines, p, pixRange;
		var nfrac, d, graphmin, graphmax, range;
		var nDecades, first, step, tick, expRangeIsValid, expRangeIsPositive, roundFactor;
		var sectionPixRange, numTicksThisSection, thisMin, thisMax, nfractmp, nfracarr;

		pixRange = abs(pixelMax - pixelMin);
		lines = [];
		nfracarr = [];

		spec.warp.class.switch(
			LinearWarp, {
				avgPixDistance ?? {avgPixDistance = 64};
				numTicks ?? {
					numTicks = (pixRange / avgPixDistance);
					numTicks = numTicks.max(3).round(1);
				};
				# graphmin, graphmax, nfrac, d = this.ideals(valueMin, valueMax, numTicks);
				if(d != inf,{
					forBy(graphmin,graphmax + (0.5*d),d,{ arg tick;
						if(tick.inclusivelyBetween(valueMin,valueMax),{
							lines = lines.add( tick );
						})
					});
				});
			},
			ExponentialWarp, {
				expRangeIsValid = ((valueMin > 0) && (valueMax > 0)) || ((valueMin < 0) && (valueMax < 0));
				if(expRangeIsValid) {
					expRangeIsPositive = valueMin > 0;
					if(expRangeIsPositive) {
						nDecades = log10(valueMax/valueMin);
						first = step = 10**(valueMin.abs.log10.trunc);
						roundFactor = step;
					} {
						nDecades = log10(valueMin/valueMax);
						step = 10**(valueMin.abs.log10.trunc - 1);
						first = 10 * step.neg;
						roundFactor = 10**(valueMax.abs.log10.trunc);
					};
					// "step:".post; step.postln;
					if(nDecades < 1) { //workaround for small ranges
						step = step * 0.1;
						roundFactor = roundFactor * 0.1;
						nfrac = valueMin.abs.log10.floor.neg + 1;
					};
					// "step:".post; step.postln;
					// "roundFactor: ".post; roundFactor.postln;
					avgPixDistance ?? {avgPixDistance = 64};
					numTicks ?? {numTicks = (pixRange / (avgPixDistance * nDecades.min(1)))};
					// nDecades = log10(valueMax/valueMin);
					// avgPixDistance ?? {avgPixDistance = 40};
					// numTicks ?? {numTicks = (pixRange / (avgPixDistance*nDecades))}; //this should be calculated for the smalles deistance between ticks...
				// "numTicks: ".post; numTicks.postln;
					// first = step = 10**(valueMin.abs.log10.trunc);
					tick = first;
					while ({tick <= (valueMax + step)}) {
						// "tick: ".post; tick.postln;
						// "tick.log10: ".post; tick.log10.postln;
						if(round(tick,roundFactor).inclusivelyBetween(valueMin,valueMax),{
							// "gl.niceNum(tick,true): ".post; gl.niceNum(tick,true).postln;
							// "numTicks: ".post; numTicks.postln;
							if(
								(numTicks > 4) ||
								((numTicks > 2).and(tick.abs==this.niceNum(tick.abs,true))) ||
								(tick.abs.log10.frac < 0.01)
							) { lines = lines.add( tick ) };
						});
						// "tick: ".post; tick.post; " (step*10): ".post; (step*10).postln;
						// if(tick >= (gl.niceNum(step*10,false)), { "is larger".postln; step = (step*10) });
						// if(round(tick,first) >= (round(step*10,first)), {
							// "is larger".postln;
						// step = (step*10) });
						if(expRangeIsPositive) {
							if((round(tick,roundFactor) >= (round(step*10,roundFactor))) && (nDecades > 1)) { step = (step*10) };
						} {
						if((round(tick.abs,roundFactor) <= (round(step,roundFactor))) && (nDecades > 1)) { step = (step*0.1).postln };
						};
						// if(tick >= (step*10), { "is larger".postln; step = (step*10) });
						// tick = (gl.niceNum(tick+step,false));//.postln;
						tick = (tick+step);//.postln;
					};
					nfracarr = lines.collect({ arg val;
						val.abs.log10.floor.neg.max(0)
					});

					// 	var nextVal = lines[inc+1] ? (10*val);
					// 	this.niceNum(nextVal - val, true).log10.floor.neg.max(0)
					// 	// this.niceNum(val, true).log10.floor.neg.max(0)
					// });
				} {
					format("Unable to get exponential GridLines for values between % and %", valueMin, valueMax).warn;
				};
			},
			{
				avgPixDistance ?? {avgPixDistance = 40};
				numTicks ?? {
					numTicks = (pixRange / avgPixDistance);
					numTicks = numTicks.max(3).round(1);
				};
				# graphmin, graphmax, nfractmp, d = this.ideals(valueMin, valueMax, numTicks);
				if(d != inf,{
					forBy(graphmin,graphmax + (0.5*d),d,{ arg tick;
						if(tick.inclusivelyBetween(valueMin,valueMax),{
							// var thisVal = this.niceNum(spec.map(tick.linlin(valueMin, valueMax, 0, 1)), false);
							var thisVal = spec.map(tick.linlin(valueMin, valueMax, 0, 1));
							thisVal = this.niceNumWarp(thisVal, spec.warp.curve ? 0);
							// lines = lines.add( spec.map(tick.linlin(valueMin, valueMax, 0, 1)) );
							if((thisVal != lines.last) && (thisVal <= valueMax)) {
								lines = lines.add( thisVal );
							}
						})
					});
				});
				nfracarr = lines.collect({ arg val, inc;
					// var nextVal = lines[inc+1] ? (10*val);
					// this.niceNum(nextVal - val, false).log10.floor.neg.max(0)
					if(val.abs.frac < 0.00001) {
						0
					} {
						val.asString.split($.)[1].size; //that's lame? but...
					}
					// (nextVal - val).log10.floor.neg.max(0);
					// this.niceNum(val, true).log10.floor.neg.max(0)
				});
			},
			// { //all other warps
			// 	avgPixDistance ?? {avgPixDistance = 64};
			// 	numTicks ?? {
			// 		numTicks = (pixRange / avgPixDistance);
			// 		numTicks = numTicks.max(3).round(1);
			// 	};
			// 	// "frst [valueMin, valueMax, numTicks]: ".post; [valueMin, valueMax, numTicks].postln;
			// 	# graphmin, graphmax, nfractmp, d = this.ideals(valueMin, valueMax, numTicks);
			// 	// "first [graphmin, graphmax, nfrac, d]: ".post; [graphmin, graphmax, nfrac, d].postln;
			// 	if(d != inf) {
			// 		forBy(graphmin,graphmax + (0.5*d),d,{ arg tick;
			// 			// "----".postln;
			// 			// "tick before everything: ".post; tick.postln;
			// 			if(lines.last.isNil && (tick != valueMin)) {lines = lines.add( valueMin )};
			// 			// if(tick.inclusivelyBetween(valueMin,valueMax)) {
			// 			if(tick >= valueMin) {
			// 				// "-".postln;
			// 				tick = tick.min(valueMax);
			// 				if(lines.last.notNil) {
			// 					thisMin = lines.last;
			// 					thisMax = tick;
			// 					sectionPixRange = (spec.unmap(thisMax) - spec.unmap(thisMin)) * pixRange;//
			// 					numTicksThisSection = sectionPixRange / (avgPixDistance * 0.2);
			// 					// "[thisMin, thisMax, numTicksThisSection]: ".post; [thisMin, thisMax, numTicksThisSection].postln;
			// 				} {
			// 					numTicksThisSection = 0;
			// 				};
			// 				if((numTicksThisSection > 2) && ((spec.warp.asSpecifier ? 0).abs > 2)) {
			// 					# graphmin, graphmax, nfractmp, d = this.ideals(thisMin, thisMax, numTicksThisSection);
			// 					// "second [graphmin, graphmax, nfrac, d]: ".post; [graphmin, graphmax, nfrac, d].postln;
			// 					if(d != inf) {
			// 						forBy(graphmin,graphmax + (0.5*d),d,{ arg tick;
			// 							// if(tick.exclusivelyBetween(graphmin,graphmax)) {
			// 							if(tick > graphmin) {
			// 								// var tickDiff;
			// 								// "more: ".post; tick.postln;
			//
			// 								sectionPixRange = (spec.unmap(tick) - spec.unmap(lines.last)) * pixRange;
			// 								// "sectionPixRange: ".post; sectionPixRange.postln;
			// 								// "tick.log10.frac: ".post; tick.log10.frac.postln;
			// 								if((sectionPixRange > (avgPixDistance * 0.5)) || (tick.log10.frac < 0.01) || (tick == 0.0)) {
			// 									if(sectionPixRange < (avgPixDistance * 0.5)) {lines.pop}; //remove previous element if it's too close
			// 									// "adding tick ".post; tick.postln;
			// 									lines = lines.add( tick );
			// 								}
			// 							}
			// 						});
			// 					};
			// 				} {
			// 					lines.last !? {
			// 						sectionPixRange = (spec.unmap(tick) - spec.unmap(lines.last)) * pixRange;
			// 					};								// "lone sectionPixRange: ".post; sectionPixRange.postln;
			// 					// "lone tick.log10.frac: ".post; tick.log10.frac.postln;
			// 					if((lines.last.isNil) || ((sectionPixRange ? 0) > (avgPixDistance * 0.5)) || (tick.abs.log10.frac < 0.01)) {
			// 						// "adding lone tick: ".post; tick.postln;
			// 						lines = lines.add( tick );
			// 					}
			// 				}
			// 			}
			// 		});
			// 	};
			// 	// calculate individual nfrac.... this causes rounding errors I think.
			// 	nfracarr = lines.collect({ arg val, inc;
			// 		// var nextVal = lines[inc+1] ? (10*val);
			// 		// this.niceNum(nextVal - val, false).log10.floor.neg.max(0)
			// 		if(val.abs.frac < 0.00001) {
			// 			0
			// 		} {
			// 			val.asString.split($.)[1].size; //that's lame? but...
			// 		}
			// 		// (nextVal - val).log10.floor.neg.max(0);
			// 		// this.niceNum(val, true).log10.floor.neg.max(0)
			// 	});
			//
			//
			// 	// if(
			// 	// (valueMin < valueMax) && (log10(valueMax/valueMin) > 1) ||
			// 	// ((valueMin > valueMax) && (log10(valueMin/valueMax) > 1))
			// 	// ) { "unsetting".postln; nfrac = nil }; //unset nfrac if more than one decade
			// }
		);

		p = ();
		p['lines'] = lines;
		if(pixRange / numTicks > 9) {
			p['labels'] = lines.collect({ arg val, inc;
				// (spec.warp.asSpecifier == \exp).if({nfrac = max(floor(log10(val)).neg, 0)}); //for \exp warp nfrac needs to be calculated for each value
				// nfrac ?? {nfrac = max(floor(log10(val)).neg, 0)}; //for \exp warp nfrac needs to be calculated for each value
				[val, this.formatLabel(val, nfrac ? nfracarr[inc] ) ? 1] });
		};
		p.cs.postln;
		^p
	}
	formatLabel { arg val, numDecimalPlaces;
		// spec.warp.asSpecifier.switch(
		// 	nil, { //FIXME this doesn't work well - why? what are the problems?
		// 		var valLog = val.log10;
		// 		// postf("val % valLog %\n", val, valLog);
		// 		// (valLog.frac < 0.01).if({
		// 		val = val.round(10**(valLog.round))
		// 		// }, {
		// 		// strUnit = "";
		// 		// (val!=this.niceNum(val,false)).if({ val = "" });
		// 		// });
		// 	}, {
				val = val.round((10**numDecimalPlaces).reciprocal);
	// }
	// );
		((numDecimalPlaces.asInteger == 0) && val.isKindOf(SimpleNumber)).if({val = val.asInteger});
		^(val.asString + (spec.units?""))
	}

}


BlankGridLines : GridLines {

	getParams {
		^()
	}
}


+ Nil {
	asGrid { ^BlankGridLines.new }
}
