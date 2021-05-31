package image

case class Operation(f: RGB => RGB) {

  def apply(rgb: RGB): RGB = f(rgb)
  def apply(p: Pixel): Pixel = Pixel(p.x, p.y, f(p.rgb))

}
