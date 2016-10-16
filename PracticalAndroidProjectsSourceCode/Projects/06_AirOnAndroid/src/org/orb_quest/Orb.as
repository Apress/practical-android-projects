package org.orb_quest
{
	import spark.components.Group;
	import spark.primitives.BitmapImage;
	
	public class Orb extends Group
	{
		public var image:BitmapImage;
		
		public function Orb(image:BitmapImage)
		{
			super();
			this.image = image;
			image.smooth = true;
			
			image.x = 512/-2;
			image.y = 512/-2;
			addElement(image);
			
			this.mouseEnabled = false;
			
		}
		public function exampleFunction():String{
			return "example function";
		}
	}
}