/**
 * 
 */

var android;
var canvasSize;

var orb_images = [];

var score = 0;
var highScore = 0;
var scoreElement;
var highScoreElement;

var ctx;
var sprites = [];
var trans = [];
var currentTick = 0;

var coord = [];
var coordCount = 5;

var selectedOrb;

var oneFifth = 1.0/5.0;
var oneTenth = 1.0/10.0;


function onLoad(){
	var red_orb = new Image();
	red_orb.src = "images/red_orb.png";
	orb_images.push(red_orb);
	
	var blue_orb = new Image();
	blue_orb.src = "images/blue_orb.png";
	orb_images.push(blue_orb);
	
	var green_orb = new Image();
	green_orb.src = "images/green_orb.png";
	orb_images.push(green_orb);
	
	scoreElement = document.getElementById("score");
	highScoreElement = document.getElementById("highScore");
	
	
	highScore = getHighScore();
	highScoreElement.innerHTML = highScore;
	
	var canvas = document.getElementById("canvas");
	var screenWidth = getScreenWidth();
	var screenHeight = getScreenHeight();
	
	if (screenWidth < screenHeight){
		canvasSize = screenWidth;
		var top = (screenHeight-canvasSize)/2.0;
		canvas.style.top = top + "px";
	} else {
		canvasSize = screenHeight;
		var left = (screenWidth-canvasSize)/2.0
		canvas.style.left = left + "px";
	}
	canvas.setAttribute("width", canvasSize);
	canvas.setAttribute("height", canvasSize);
	
	canvas.addEventListener("click", canvasClick, false);
	
	ctx = canvas.getContext("2d");

	
	for(var i=0;i<coordCount;i++){
		coord.push(oneFifth*i+oneTenth);
	}
	for (var col=0;col<coordCount;col++){
		for (var row=0;row<coordCount;row++){
			sprites.push(new Sprite("Orb", randomOrbImage(), coord[col], coord[row], oneFifth));
		}
	}
	
	setInterval("renderScene()", 1000/30);
}

function canvasClick(e){
	if (trans.length == 0){
		if (selectedOrb){
				var secondOrb = findOrbForXY(e.layerX, e.layerY);
				if (secondOrb != selectedOrb){
				
					var endOfScale = currentTick + 15
					trans.push(new Transformation(currentTick, endOfScale,oneFifth,oneTenth,"scale","windupovershoot",secondOrb));
					
					var endOfTranslate = endOfScale + 15;
					trans.push(new Transformation(endOfScale, endOfTranslate,secondOrb.centerX,selectedOrb.centerX,"centerX","easeboth",secondOrb, null));
					trans.push(new Transformation(endOfScale, endOfTranslate,secondOrb.centerY,selectedOrb.centerY,"centerY","easeboth",secondOrb, null));
					trans.push(new Transformation(endOfScale, endOfTranslate,selectedOrb.centerX,secondOrb.centerX,"centerX","easeboth",selectedOrb, null));
					trans.push(new Transformation(endOfScale, endOfTranslate,selectedOrb.centerY,secondOrb.centerY,"centerY","easeboth",selectedOrb, null));
					
					trans.push(new Transformation(endOfTranslate, endOfTranslate+15,oneTenth,oneFifth,"scale","windupovershoot",secondOrb, null));
					trans.push(new Transformation(endOfTranslate, endOfTranslate+15,oneTenth,oneFifth,"scale","windupovershoot",selectedOrb, "checkForGroups()"));
					
					var indexA = sprites.indexOf(secondOrb);
					var indexB = sprites.indexOf(selectedOrb);
					
					sprites[indexB] = secondOrb;
					sprites[indexA] = selectedOrb;
					
					selectedOrb = null;
			}
		} else {
			selectedOrb = findOrbForXY(e.layerX, e.layerY);
			trans.push(new Transformation(currentTick, currentTick + 15,oneFifth,oneTenth,"scale","windupovershoot",selectedOrb, null));
		}
	}
}

function checkForGroups(){
	var animatedOrbs = [];
	
	var endScale = currentTick+15
	var endTrans = endScale+15;
	var matchsFound = 0;
	
	//check rows
	for (var r=0;r<coordCount;r++){
		var allSame = true;
		var color0 = orbForColRow(0, r).img;
		for (var c=1;c<coordCount;c++){
			var colorC = orbForColRow(c, r).img;
			if (color0 != colorC){
				allSame = false;
				break;
			}
		}
		if (allSame){
			matchsFound++;
			for (var c=0;c<coordCount;c++){
				var orb = orbForColRow(c, r);
				trans.push(new Transformation(currentTick, endScale,orb.scale,oneTenth,"scale","windupovershoot",orb));
				trans.push(new Transformation(endScale, endTrans,orb.centerX,orb.centerX+1.0,"centerX","easeboth",orb, "newOrbAt("+c+","+r+")"));
			}
		}
	}
	//check cols
	for (var c=0;c<coordCount;c++){
		var allSame = true;
		var color0 = orbForColRow(c, 0).img;
		for (var r=0;r<coordCount;r++){
			var colorC = orbForColRow(c, r).img;
			if (color0 != colorC){
				allSame = false;
				break;
			}
		}
		if (allSame){
			matchsFound++;
			for (var r=0;r<coordCount;r++){
				var orb = orbForColRow(c, r);
				trans.push(new Transformation(currentTick, endScale,orb.scale,oneTenth,"scale","windupovershoot",orb));
				trans.push(new Transformation(endScale, endTrans,orb.centerY,orb.centerY+1.0,"centerY","easeboth",orb, "newOrbAt("+c+","+r+")"));
			}
		}
	}
	if (matchsFound > 0){
		trans.push(new Transformation(endScale, endTrans,null,null,null,null,null, "endCheck("+matchsFound+")"));
	}
}

function endCheck(numFound){
	score += numFound;
	scoreElement.innerHTML = score;
	checkForGroups();
	if (score > highScore){
		highScore = score;
		highScoreElement.innerHTML = highScore;
		setHighScore(highScore);
	}
}

function newOrbAt(col, row){
	var index = col*coordCount + row;
	var newOrb = new Sprite("Orb", randomOrbImage(), coord[col], coord[row], oneFifth);
	sprites[index] = newOrb;
}

function renderScene(){
	ctx.fillStyle = "rgb(256,256,256)"; 
	ctx.fillRect(0,0,canvasSize,canvasSize);
	
	var oldTransIndex = [];
	
	for (var i=0;i<trans.length;i++){
		var tran = trans[i];
		if (tran.endTick >= currentTick){
			applyTransformation(tran, currentTick);
		} else {
			oldTransIndex.push(i);
		}
	}
	oldTransIndex.reverse();
	for (var i=0;i<oldTransIndex.length;i++){
		trans.splice(oldTransIndex[i], 1);
	}
	
	for (var i=0;i<sprites.length;i++){
		renderSprite(ctx, sprites[i]);
	}
	currentTick++;
}

function applyTransformation(trans, tick){
	if (tick >= trans.startTick && tick <= trans.endTick){
		if (trans.sprite){
			var fraction = (tick-trans.startTick)/(trans.endTick - trans.startTick);
			fraction = eval(trans.tweenFunction + "(" + fraction + ")");
			var value = trans.startValue + (trans.endValue - trans.startValue)*fraction;
			var expression = "trans.sprite." + trans.field + " = " + value;
			eval(expression);
		}
		if (tick == trans.endTick){
			if (trans.whenDone){
				eval(trans.whenDone);	
			}
		}
	}
}

function renderSprite(ctx, sprite){
	ctx.save();
	
	var widthInPixels = canvasSize*sprite.scale;
	var scale = widthInPixels/sprite.img.width;
	var centerX = canvasSize*sprite.centerX-(widthInPixels/2.0);
	var centerY = canvasSize*sprite.centerY-(widthInPixels/2.0);
	
	ctx.translate(centerX, centerY);
	ctx.scale(scale, scale);
	ctx.drawImage(sprite.img, 0, 0);
	
	ctx.restore();
}

function randomOrbImage(){
	return orb_images[Math.floor(Math.random()*orb_images.length)];
}

function findOrbForXY(x,y){
	var col = boxFromXorY(x);
	var row = boxFromXorY(y);
	return orbForColRow(col, row);
}

function boxFromXorY(value){
	var ratio = value/canvasSize;
	for (var i=0;i<coord.length;i++){
		if (ratio < coord[i]+coord[0]){
			return i;
		}
	}
	return coord.length-1;//default to last box.
}

function orbForColRow(col, row){
	var index = col*coordCount + row; 
	return sprites[index];
}

//ANIMATION AND STUFF//

function Sprite(type, img, centerX, centerY, scale) {
	this.type = type;
	this.img = img;
	this.centerX = centerX;
	this.centerY = centerY;
	this.scale = scale;
}

function Transformation(startTick,endTick,startValue,endValue,field,tweenFunction,sprite, whenDone) {
	this.startTick = startTick;
	this.endTick = endTick;
	this.startValue = startValue;
	this.endValue = endValue;
	this.field = field;
	this.tweenFunction = tweenFunction;
	this.sprite = sprite;
	this.whenDone = whenDone;
}


function linear(fraction){
	return fraction;
}

function windupovershoot(fraction){
	return -4.586466*Math.pow(fraction, 3)+6.842106*Math.pow(fraction, 2)+-1.25564*fraction;
}

function easeboth(fraction){
	return -2.3158484*Math.pow(fraction, 3)+3.5488129*Math.pow(fraction, 2)+-0.23296452*Math.pow(fraction, 1);
}

///PLATFORM SPECIFIC//
function getScreenWidth(){
	if (android){
		return android.getScreenWidth();
	} else {
		return 320;
	}
}
function getScreenHeight(){
	if (android){
		return android.getScreenHeight();
	} else {
		return 480;
	}
}
function setHighScore(value){
	if (localStorage && typeof(localStorage) != 'undefined') {
		localStorage.setItem("HighScore", value);
		return value;
	} else {
		if (android){
			android.setHighScore(value);
			return value;
		}
	}
	//not actually saved
	return value;
}
function getHighScore(){
	if (localStorage && typeof(localStorage) != 'undefined') {
		var value = localStorage.getItem("HighScore");
		if (value) {
			return parseInt(value);
		}
		//Maybe it is not set yet.
		return 0;
	} else {
		if (android){
			return android.getHighScore();
		}
	}
	//no local storage, not on android... just return zero.
	return 0;
}
