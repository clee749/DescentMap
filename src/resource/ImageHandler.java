package resource;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

import mapobject.ProximityBomb;
import mapobject.powerup.Powerup;
import mapobject.unit.Unit;
import util.MapUtils;

import common.ObjectType;

// Powerups: http://old.r1ch.net/old/descent/weapons.htm
// Robots: http://www.descent2.com/goodies/3dmodels/thinman/descent1

/**
 * Adapted from http://flyingdogz.wordpress.com/2008/02/11/image-rotate-in-java-2-easier-to-use/
 */
class ImageUtils {
  public static Image rotateImage(Image img, double direction) {
    BufferedImage bufImg = toBufferedImage(img);
    return tilt(bufImg, direction);
  }

  public static BufferedImage tilt(BufferedImage image, double angle) {
    double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
    int w = image.getWidth(), h = image.getHeight();
    int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
    GraphicsConfiguration gc = getDefaultConfiguration();
    BufferedImage result = gc.createCompatibleImage(neww, newh, Transparency.TRANSLUCENT);
    Graphics2D g = result.createGraphics();
    g.translate((neww - w) / 2, (newh - h) / 2);
    g.rotate(angle, w / 2, h / 2);
    g.drawRenderedImage(image, null);
    g.dispose();
    return result;
  }

  public static GraphicsConfiguration getDefaultConfiguration() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    return gd.getDefaultConfiguration();
  }

  // http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
  // An Image object cannot be converted to a BufferedImage object.
  // The closest equivalent is to create a buffered image and then draw the image on the buffered
  // image.
  // This method returns a buffered image with the contents of an image
  public static BufferedImage toBufferedImage(Image image) {
    if (image instanceof BufferedImage) {
      return (BufferedImage) image;
    }

    // Determine if the image has transparent pixels; for this method's
    // implementation, see e661 Determining If an Image Has Transparent Pixels
    boolean hasAlpha = hasAlpha(image);

    // Create a buffered image with a format that's compatible with the screen
    BufferedImage bimage = null;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    try {
      // Determine the type of transparency of the new buffered image
      int transparency = Transparency.OPAQUE;
      if (hasAlpha) {
        transparency = Transparency.BITMASK;
      }

      // Create the buffered image
      GraphicsDevice gs = ge.getDefaultScreenDevice();
      GraphicsConfiguration gc = gs.getDefaultConfiguration();
      bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
    }
    catch (HeadlessException e) {
      // The system does not have a screen
    }

    if (bimage == null) {
      // Create a buffered image using the default color model
      int type = BufferedImage.TYPE_INT_RGB;
      if (hasAlpha) {
        type = BufferedImage.TYPE_INT_ARGB;
      }
      bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
    }

    // Copy image to buffered image
    Graphics g = bimage.createGraphics();

    // Paint the image onto the buffered image
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return bimage;
  }

  // http://www.exampledepot.com/egs/java.awt.image/HasAlpha.html
  // This method returns true if the specified image has transparent pixels
  public static boolean hasAlpha(Image image) {
    // If buffered image, the color model is readily available
    if (image instanceof BufferedImage) {
      BufferedImage bimage = (BufferedImage) image;
      return bimage.getColorModel().hasAlpha();
    }

    // Use a pixel grabber to retrieve the image's color model;
    // grabbing a single pixel is usually sufficient
    PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
    try {
      pg.grabPixels();
    }
    catch (InterruptedException e) {}

    // Get the image's color model
    ColorModel cm = pg.getColorModel();
    return cm.hasAlpha();
  }
}


public class ImageHandler {
  public static final String DEFAULT_PATH = "/resrc/images";
  public static final int NUM_IMAGES_IN_QUADRANT = 10;
  public static final double SHOT_IMAGE_RADIUS = 0.12;
  public static final int NUM_IMAGES_IN_CIRCLE = NUM_IMAGES_IN_QUADRANT * 4;
  public static final double RADIANS_PER_IMAGE = MapUtils.PI_OVER_TWO / NUM_IMAGES_IN_QUADRANT;

  private HashMap<String, ArrayList<Image>> images;

  public void loadImages(String path, int pixels_per_cell) throws IOException {
    System.out.print("Loading images...");
    images = new HashMap<String, ArrayList<Image>>();
    for (ObjectType type : ObjectType.POWERUPS) {
      loadAnimatedGif(path, type.name(), pixels_per_cell, Powerup.RADIUS);
    }
    for (ObjectType type : ObjectType.SCENERIES) {
      loadAnimatedGif(path, type.name(), pixels_per_cell, 0.5);
    }
    for (ObjectType type : ObjectType.SHOTS) {
      if (type.equals(ObjectType.LaserShot)) {
        for (int level = 1; level <= 4; ++level) {
          loadRotatedImages(path, type.name() + level, pixels_per_cell, SHOT_IMAGE_RADIUS);
        }
      }
      else {
        loadRotatedImages(path, type.name(), pixels_per_cell, SHOT_IMAGE_RADIUS);
      }
    }
    for (ObjectType type : ObjectType.STANDARD_ROBOTS) {
      Double radius = Unit.getRadius(type);
      if (radius != null) {
        loadRotatedImages(path, type.name(), pixels_per_cell, radius);
      }
    }
    for (ObjectType type : ObjectType.BOSS_ROBOTS) {
      loadRotatedImages(path, type.name(), pixels_per_cell, Unit.getRadius(type));
      loadRotatedImages(path, type.name() + "Cloaked", pixels_per_cell, Unit.getRadius(type));
    }
    loadRotatedImages(path, "Pyro", pixels_per_cell, Unit.getRadius(ObjectType.Pyro));
    loadAnimatedGif(path, "ProximityBomb", pixels_per_cell, ProximityBomb.RADIUS);
    System.out.println("Done!");
  }

  public void loadImages(int pixels_per_cell) throws IOException {
    loadImages(DEFAULT_PATH, pixels_per_cell);
  }

  public void loadAnimatedGif(String path, String name, int pixels_per_cell, double radius)
          throws IOException {
    String filename = path + "/" + name + ".gif";
    InputStream is = getClass().getResourceAsStream(filename);
    if (is == null) {
      throw new RuntimeException("No resource with this name was found: " + filename);
    }
    ImageInputStream iis = ImageIO.createImageInputStream(is);
    if (iis == null) {
      throw new RuntimeException("No suitable ImageInputStreamSpi exists");
    }
    ArrayList<Image> list = new ArrayList<Image>();
    Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
    if (!readers.hasNext()) {
      throw new RuntimeException("No ImageReader found");
    }
    ImageReader reader = readers.next();
    reader.setInput(iis);
    int n = reader.getNumImages(true);
    for (int i = 0; i < n; i++) {
      Image current = reader.read(i);
      if (radius > 0) {
        current = scaleImage(current, pixels_per_cell, radius);
      }
      list.add(current);
    }
    images.put(name, list);
    iis.close();
    is.close();
  }

  public void loadRotatedImages(String path, String name, int pixels_per_cell, double radius)
          throws IOException {
    String filename = path + "/" + name + ".gif";
    InputStream is = getClass().getResourceAsStream(filename);
    if (is == null) {
      throw new RuntimeException("No resource with this name was found: " + filename);
    }
    Image image = ImageIO.read(is);
    if (image == null) {
      throw new RuntimeException("No registered ImageReader claims to be able to read the resulting stream");
    }
    image = scaleImage(image, pixels_per_cell, radius);
    ArrayList<Image> list = new ArrayList<Image>();
    list.add(image);
    for (int i = 1; i < ImageHandler.NUM_IMAGES_IN_CIRCLE; ++i) {
      list.add(ImageUtils.rotateImage(image, i * ImageHandler.RADIANS_PER_IMAGE));
    }
    images.put(name, list);
    is.close();
  }

  public Image scaleImage(Image image, int pixels_per_cell, double radius) {
    // The ImageIcon ensures that all the pixels in the image are loaded for width and height
    image = new ImageIcon(image).getImage();
    int size = (int) (pixels_per_cell * 2 * radius);
    if (image.getWidth(null) < image.getHeight(null)) {
      return image.getScaledInstance(-1, size, Image.SCALE_SMOOTH);
    }
    return image.getScaledInstance(size, -1, Image.SCALE_SMOOTH);
  }

  public Image getImage(String name, int frame) {
    ArrayList<Image> list = images.get(name);
    return list.get(frame % list.size());
  }

  public Image getImage(String name, double direction) {
    return images.get(name).get(
            (int) (Math.round(direction / ImageHandler.RADIANS_PER_IMAGE)) %
                    ImageHandler.NUM_IMAGES_IN_CIRCLE);
  }

  public Image getImage(String name) {
    return images.get(name).get(0);
  }
}
