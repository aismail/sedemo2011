/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-09 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

package processing.core;

import java.awt.*;
import java.util.HashMap;


/**
 * Main graphics and rendering context, as well as the base API implementation for processing "core".
 * Use this class if you need to draw into an off-screen graphics buffer.
 * A PGraphics object can be constructed with the <b>createGraphics()</b> function.
 * The <b>beginDraw()</b> and <b>endDraw()</b> methods (see above example) are necessary to set up the buffer and to finalize it.
 * The fields and methods for this class are extensive;
 * for a complete list visit the developer's reference: http://dev.processing.org/reference/core/
 * =advanced
 * Main graphics and rendering context, as well as the base API implementation.
 *
 * <h2>Subclassing and initializing PGraphics objects</h2>
 * Starting in release 0149, subclasses of PGraphics are handled differently.
 * The constructor for subclasses takes no parameters, instead a series of
 * functions are called by the hosting PApplet to specify its attributes.
 * <ul>
 * <li>setParent(PApplet) - is called to specify the parent PApplet.
 * <li>setPrimary(boolean) - called with true if this PGraphics will be the
 * primary drawing surface used by the sketch, or false if not.
 * <li>setPath(String) - called when the renderer needs a filename or output
 * path, such as with the PDF or DXF renderers.
 * <li>setSize(int, int) - this is called last, at which point it's safe for
 * the renderer to complete its initialization routine.
 * </ul>
 * The functions were broken out because of the growing number of parameters
 * such as these that might be used by a renderer, yet with the exception of
 * setSize(), it's not clear which will be necessary. So while the size could
 * be passed in to the constructor instead of a setSize() function, a function
 * would still be needed that would notify the renderer that it was time to
 * finish its initialization. Thus, setSize() simply does both.
 *
 * <h2>Know your rights: public vs. private methods</h2>
 * Methods that are protected are often subclassed by other renderers, however
 * they are not set 'public' because they shouldn't be part of the user-facing
 * public API accessible from PApplet. That is, we don't want sketches calling
 * textModeCheck() or vertexTexture() directly.
 *
 * <h2>Handling warnings and exceptions</h2>
 * Methods that are unavailable generally show a warning, unless their lack of
 * availability will soon cause another exception. For instance, if a method
 * like getMatrix() returns null because it is unavailable, an exception will
 * be thrown stating that the method is unavailable, rather than waiting for
 * the NullPointerException that will occur when the sketch tries to use that
 * method. As of release 0149, warnings will only be shown once, and exceptions
 * have been changed to warnings where possible.
 *
 * <h2>Using xxxxImpl() for subclassing smoothness</h2>
 * The xxxImpl() methods are generally renderer-specific handling for some
 * subset if tasks for a particular function (vague enough for you?) For
 * instance, imageImpl() handles drawing an image whose x/y/w/h and u/v coords
 * have been specified, and screen placement (independent of imageMode) has
 * been determined. There's no point in all renderers implementing the
 * <tt>if (imageMode == BLAH)</tt> placement/sizing logic, so that's handled
 * by PGraphics, which then calls imageImpl() once all that is figured out.
 *
 * <h2>His brother PImage</h2>
 * PGraphics subclasses PImage so that it can be drawn and manipulated in a
 * similar fashion. As such, many methods are inherited from PGraphics,
 * though many are unavailable: for instance, resize() is not likely to be
 * implemented; the same goes for mask(), depending on the situation.
 *
 * <h2>What's in PGraphics, what ain't</h2>
 * For the benefit of subclasses, as much as possible has been placed inside
 * PGraphics. For instance, bezier interpolation code and implementations of
 * the strokeCap() method (that simply sets the strokeCap variable) are
 * handled here. Features that will vary widely between renderers are located
 * inside the subclasses themselves. For instance, all matrix handling code
 * is per-renderer: Java 2D uses its own AffineTransform, P2D uses a PMatrix2D,
 * and PGraphics3D needs to keep continually update forward and reverse
 * transformations. A proper (future) OpenGL implementation will have all its
 * matrix madness handled by the card. Lighting also falls under this
 * category, however the base material property settings (emissive, specular,
 * et al.) are handled in PGraphics because they use the standard colorMode()
 * logic. Subclasses should override methods like emissiveFromCalc(), which
 * is a point where a valid color has been defined internally, and can be
 * applied in some manner based on the calcXxxx values.
 *
 * <h2>What's in the PGraphics documentation, what ain't</h2>
 * Some things are noted here, some things are not. For public API, always
 * refer to the <a href="http://processing.org/reference">reference</A>
 * on Processing.org for proper explanations. <b>No attempt has been made to
 * keep the javadoc up to date or complete.</b> It's an enormous task for
 * which we simply do not have the time. That is, it's not something that
 * to be done once&mdash;it's a matter of keeping the multiple references
 * synchronized (to say nothing of the translation issues), while targeting
 * them for their separate audiences. Ouch.
 *
 * We're working right now on synchronizing the two references, so the website reference
 * is generated from the javadoc comments. Yay.
 *
 * @webref rendering
 * @instanceName graphics any object of the type PGraphics
 * @usage Web &amp; Application
 * @see processing.core.PApplet#createGraphics(int, int, String)
 */
public class PGraphics extends PImage implements PConstants {

  // ........................................................

  // width and height are already inherited from PImage


  /// width minus one (useful for many calculations)
  protected int width1;

  /// height minus one (useful for many calculations)
  protected int height1;

  /// width * height (useful for many calculations)
  public int pixelCount;

  /// true if smoothing is enabled (read-only)
  public boolean smooth = false;

  // ........................................................

  /// true if defaults() has been called a first time
  protected boolean settingsInited;

  /// set to a PGraphics object being used inside a beginRaw/endRaw() block
  protected PGraphics raw;

  // ........................................................

  /** path to the file being saved for this renderer (if any) */
  protected String path;

  /**
   * true if this is the main drawing surface for a particular sketch.
   * This would be set to false for an offscreen buffer or if it were
   * created any other way than size(). When this is set, the listeners
   * are also added to the sketch.
   */
  protected boolean primarySurface;

  // ........................................................

  /**
   * Array of hint[] items. These are hacks to get around various
   * temporary workarounds inside the environment.
   * <p/>
   * Note that this array cannot be static, as a hint() may result in a
   * runtime change specific to a renderer. For instance, calling
   * hint(DISABLE_DEPTH_TEST) has to call glDisable() right away on an
   * instance of PGraphicsOpenGL.
   * <p/>
   * The hints[] array is allocated early on because it might
   * be used inside beginDraw(), allocate(), etc.
   */
  protected boolean[] hints = new boolean[HINT_COUNT];


  ////////////////////////////////////////////////////////////

  // STYLE PROPERTIES

  // Also inherits imageMode() and smooth() (among others) from PImage.

  /** The current colorMode */
  public int colorMode; // = RGB;

  /** Max value for red (or hue) set by colorMode */
  public float colorModeX; // = 255;

  /** Max value for green (or saturation) set by colorMode */
  public float colorModeY; // = 255;

  /** Max value for blue (or value) set by colorMode */
  public float colorModeZ; // = 255;

  /** Max value for alpha set by colorMode */
  public float colorModeA; // = 255;

  /** True if colors are not in the range 0..1 */
  boolean colorModeScale; // = true;

  /** True if colorMode(RGB, 255) */
  boolean colorModeDefault; // = true;

  // ........................................................

  // Tint color for images

  /**
   * True if tint() is enabled (read-only).
   *
   * Using tint/tintColor seems a better option for naming than
   * tintEnabled/tint because the latter seems ugly, even though
   * g.tint as the actual color seems a little more intuitive,
   * it's just that g.tintEnabled is even more unintuitive.
   * Same goes for fill and stroke, et al.
   */
  public boolean tint;

  /** tint that was last set (read-only) */
  public int tintColor;

  protected boolean tintAlpha;
  protected float tintR, tintG, tintB, tintA;
  protected int tintRi, tintGi, tintBi, tintAi;

  // ........................................................

  // Fill color

  /** true if fill() is enabled, (read-only) */
  public boolean fill;

  /** fill that was last set (read-only) */
  public int fillColor = 0xffFFFFFF;

  protected boolean fillAlpha;
  protected float fillR, fillG, fillB, fillA;
  protected int fillRi, fillGi, fillBi, fillAi;

  // ........................................................

  // Stroke color

  /** true if stroke() is enabled, (read-only) */
  public boolean stroke;

  /** stroke that was last set (read-only) */
  public int strokeColor = 0xff000000;

  protected boolean strokeAlpha;
  protected float strokeR, strokeG, strokeB, strokeA;
  protected int strokeRi, strokeGi, strokeBi, strokeAi;

  // ........................................................

  // Additional stroke properties

  static protected final float DEFAULT_STROKE_WEIGHT = 1;
  static protected final int DEFAULT_STROKE_JOIN = MITER;
  static protected final int DEFAULT_STROKE_CAP = ROUND;

  /**
   * Last value set by strokeWeight() (read-only). This has a default
   * setting, rather than fighting with renderers about whether that
   * renderer supports thick lines.
   */
  public float strokeWeight = DEFAULT_STROKE_WEIGHT;

  /**
   * Set by strokeJoin() (read-only). This has a default setting
   * so that strokeJoin() need not be called by defaults,
   * because subclasses may not implement it (i.e. PGraphicsGL)
   */
  public int strokeJoin = DEFAULT_STROKE_JOIN;

  /**
   * Set by strokeCap() (read-only). This has a default setting
   * so that strokeCap() need not be called by defaults,
   * because subclasses may not implement it (i.e. PGraphicsGL)
   */
  public int strokeCap = DEFAULT_STROKE_CAP;

  // ........................................................

  // Shape placement properties

  // imageMode() is inherited from PImage

  /** The current rect mode (read-only) */
  public int rectMode;

  /** The current ellipse mode (read-only) */
  public int ellipseMode;

  /** The current shape alignment mode (read-only) */
  public int shapeMode;

  /** The current image alignment (read-only) */
  public int imageMode = CORNER;

  // ........................................................

  // Text and font properties

  /** The current text font (read-only) */
  public PFont textFont;

  /** The current text align (read-only) */
  public int textAlign = LEFT;

  /** The current vertical text alignment (read-only) */
  public int textAlignY = BASELINE;

  /** The current text mode (read-only) */
  public int textMode = MODEL;

  /** The current text size (read-only) */
  public float textSize;

  /** The current text leading (read-only) */
  public float textLeading;

  // ........................................................

  // Material properties

//  PMaterial material;
//  PMaterial[] materialStack;
//  int materialStackPointer;

  public float ambientR, ambientG, ambientB;
  public float specularR, specularG, specularB;
  public float emissiveR, emissiveG, emissiveB;
  public float shininess;


  // Style stack

  static final int STYLE_STACK_DEPTH = 64;
  PStyle[] styleStack = new PStyle[STYLE_STACK_DEPTH];
  int styleStackDepth;


  ////////////////////////////////////////////////////////////


  /** Last background color that was set, zero if an image */
  public int backgroundColor = 0xffCCCCCC;

  protected boolean backgroundAlpha;
  protected float backgroundR, backgroundG, backgroundB, backgroundA;
  protected int backgroundRi, backgroundGi, backgroundBi, backgroundAi;

  // ........................................................

  /**
   * Current model-view matrix transformation of the form m[row][column],
   * which is a "column vector" (as opposed to "row vector") matrix.
   */
//  PMatrix matrix;
//  public float m00, m01, m02, m03;
//  public float m10, m11, m12, m13;
//  public float m20, m21, m22, m23;
//  public float m30, m31, m32, m33;

//  static final int MATRIX_STACK_DEPTH = 32;
//  float[][] matrixStack = new float[MATRIX_STACK_DEPTH][16];
//  float[][] matrixInvStack = new float[MATRIX_STACK_DEPTH][16];
//  int matrixStackDepth;

  static final int MATRIX_STACK_DEPTH = 32;

  // ........................................................

  /**
   * Java AWT Image object associated with this renderer. For P2D and P3D,
   * this will be associated with their MemoryImageSource. For PGraphicsJava2D,
   * it will be the offscreen drawing buffer.
   */
  public Image image;

  // ........................................................

  // internal color for setting/calculating
  protected float calcR, calcG, calcB, calcA;
  protected int calcRi, calcGi, calcBi, calcAi;
  protected int calcColor;
  protected boolean calcAlpha;

  /** The last RGB value converted to HSB */
  int cacheHsbKey;
  /** Result of the last conversion to HSB */
  float[] cacheHsbValue = new float[3];

  // ........................................................

  /**
   * Type of shape passed to beginShape(),
   * zero if no shape is currently being drawn.
   */
  protected int shape;

  // vertices
  public static final int DEFAULT_VERTICES = 512;
  protected float vertices[][] =
    new float[DEFAULT_VERTICES][VERTEX_FIELD_COUNT];
  protected int vertexCount; // total number of vertices

  // ........................................................

  protected boolean bezierInited = false;
  public int bezierDetail = 20;

  // used by both curve and bezier, so just init here
  protected PMatrix3D bezierBasisMatrix =
    new PMatrix3D(-1,  3, -3,  1,
                   3, -6,  3,  0,
                  -3,  3,  0,  0,
                   1,  0,  0,  0);

  //protected PMatrix3D bezierForwardMatrix;
  protected PMatrix3D bezierDrawMatrix;

  // ........................................................

  protected boolean curveInited = false;
  protected int curveDetail = 20;
  public float curveTightness = 0;
  // catmull-rom basis matrix, perhaps with optional s parameter
  protected PMatrix3D curveBasisMatrix;
  protected PMatrix3D curveDrawMatrix;

  protected PMatrix3D bezierBasisInverse;
  protected PMatrix3D curveToBezierMatrix;

  // ........................................................

  // spline vertices

  protected float curveVertices[][];
  protected int curveVertexCount;

  // ........................................................

  // precalculate sin/cos lookup tables [toxi]
  // circle resolution is determined from the actual used radii
  // passed to ellipse() method. this will automatically take any
  // scale transformations into account too

  // [toxi 031031]
  // changed table's precision to 0.5 degree steps
  // introduced new vars for more flexible code
  static final protected float sinLUT[];
  static final protected float cosLUT[];
  static final protected float SINCOS_PRECISION = 0.5f;
  static final protected int SINCOS_LENGTH = (int) (360f / SINCOS_PRECISION);
  static {
    sinLUT = new float[SINCOS_LENGTH];
    cosLUT = new float[SINCOS_LENGTH];
    for (int i = 0; i < SINCOS_LENGTH; i++) {
      sinLUT[i] = (float) Math.sin(i * DEG_TO_RAD * SINCOS_PRECISION);
      cosLUT[i] = (float) Math.cos(i * DEG_TO_RAD * SINCOS_PRECISION);
    }
  }

  // ........................................................

  /** The current font if a Java version of it is installed */
  //protected Font textFontNative;

  /** Metrics for the current native Java font */
  //protected FontMetrics textFontNativeMetrics;

  /** Last text position, because text often mixed on lines together */
  protected float textX, textY, textZ;

  /**
   * Internal buffer used by the text() functions
   * because the String object is slow
   */
  protected char[] textBuffer = new char[8 * 1024];
  protected char[] textWidthBuffer = new char[8 * 1024];

  protected int textBreakCount;
  protected int[] textBreakStart;
  protected int[] textBreakStop;

  // ........................................................

  public boolean edge = true;

  // ........................................................

  /// normal calculated per triangle
  static protected final int NORMAL_MODE_AUTO = 0;
  /// one normal manually specified per shape
  static protected final int NORMAL_MODE_SHAPE = 1;
  /// normals specified for each shape vertex
  static protected final int NORMAL_MODE_VERTEX = 2;

  /// Current mode for normals, one of AUTO, SHAPE, or VERTEX
  protected int normalMode;

  /// Keep track of how many calls to normal, to determine the mode.
  //protected int normalCount;

  protected boolean autoNormal;

  /** Current normal vector. */
  public float normalX, normalY, normalZ;

  // ........................................................

  /**
   * Sets whether texture coordinates passed to
   * vertex() calls will be based on coordinates that are
   * based on the IMAGE or NORMALIZED.
   */
  public int textureMode;

  /**
   * Current horizontal coordinate for texture, will always
   * be between 0 and 1, even if using textureMode(IMAGE).
   */
  public float textureU;

  /** Current vertical coordinate for texture, see above. */
  public float textureV;

  /** Current image being used as a texture */
  public PImage textureImage;

  // ........................................................

  // [toxi031031] new & faster sphere code w/ support flexibile resolutions
  // will be set by sphereDetail() or 1st call to sphere()
  float sphereX[], sphereY[], sphereZ[];

  /// Number of U steps (aka "theta") around longitudinally spanning 2*pi
  public int sphereDetailU = 0;
  /// Number of V steps (aka "phi") along latitudinally top-to-bottom spanning pi
  public int sphereDetailV = 0;


  //////////////////////////////////////////////////////////////

  // INTERNAL


  /**
   * Constructor for the PGraphics object. Use this to ensure that
   * the defaults get set properly. In a subclass, use this(w, h)
   * as the first line of a subclass' constructor to properly set
   * the internal fields and defaults.
   *
   */
  public PGraphics() {
  }


  public void setParent(PApplet parent) {  // ignore
    this.parent = parent;
  }


  /**
   * Set (or unset) this as the main drawing surface. Meaning that it can
   * safely be set to opaque (and given a default gray background), or anything
   * else that goes along with that.
   */
  public void setPrimary(boolean primary) {  // ignore
    this.primarySurface = primary;

    // base images must be opaque (for performance and general
    // headache reasons.. argh, a semi-transparent opengl surface?)
    // use createGraphics() if you want a transparent surface.
    if (primarySurface) {
      format = RGB;
    }
  }


  public void setPath(String path) {  // ignore
    this.path = path;
  }


  /**
   * The final step in setting up a renderer, set its size of this renderer.
   * This was formerly handled by the constructor, but instead it's been broken
   * out so that setParent/setPrimary/setPath can be handled differently.
   *
   * Important that this is ignored by preproc.pl because otherwise it will
   * override setSize() in PApplet/Applet/Component, which will 1) not call
   * super.setSize(), and 2) will cause the renderer to be resized from the
   * event thread (EDT), causing a nasty crash as it collides with the
   * animation thread.
   */
  public void setSize(int w, int h) {  // ignore
    width = w;
    height = h;
    width1 = width - 1;
    height1 = height - 1;

    allocate();
    reapplySettings();
  }


  /**
   * Allocate memory for this renderer. Generally will need to be implemented
   * for all renderers.
   */
  protected void allocate() { }


  /**
   * Handle any takedown for this graphics context.
   * <p>
   * This is called when a sketch is shut down and this renderer was
   * specified using the size() command, or inside endRecord() and
   * endRaw(), in order to shut things off.
   */
  public void dispose() {  // ignore
  }



  //////////////////////////////////////////////////////////////

  // FRAME


  /**
   * Some renderers have requirements re: when they are ready to draw.
   */
  public boolean canDraw() {  // ignore
    return true;
  }


  /**
   * Sets the default properties for a PGraphics object. It should be called before anything is drawn into the object.
   * =advanced
   * <p/>
   * When creating your own PGraphics, you should call this before
   * drawing anything.
   *
   * @webref
   * @brief Sets up the rendering context
   */
  public void beginDraw() {  // ignore
  }


  /**
   * Finalizes the rendering of a PGraphics object so that it can be shown on screen.
   * =advanced
   * <p/>
   * When creating your own PGraphics, you should call this when
   * you're finished drawing.
   *
   * @webref
   * @brief Finalizes the renderering context
   */
  public void endDraw() {  // ignore
  }


  public void flush() {
    // no-op, mostly for P3D to write sorted stuff
  }


  protected void checkSettings() {
    if (!settingsInited) defaultSettings();
  }


  /**
   * Set engine's default values. This has to be called by PApplet,
   * somewhere inside setup() or draw() because it talks to the
   * graphics buffer, meaning that for subclasses like OpenGL, there
   * needs to be a valid graphics context to mess with otherwise
   * you'll get some good crashing action.
   *
   * This is currently called by checkSettings(), during beginDraw().
   */
  protected void defaultSettings() {  // ignore
//    System.out.println("PGraphics.defaultSettings() " + width + " " + height);

    noSmooth();  // 0149

    colorMode(RGB, 255);
    fill(255);
    stroke(0);

    // as of 0178, no longer relying on local versions of the variables
    // being set, because subclasses may need to take extra action.
    strokeWeight(DEFAULT_STROKE_WEIGHT);
    strokeJoin(DEFAULT_STROKE_JOIN);
    strokeCap(DEFAULT_STROKE_CAP);

    // init shape stuff
    shape = 0;

    // init matrices (must do before lights)
    //matrixStackDepth = 0;

    rectMode(CORNER);
    ellipseMode(DIAMETER);

    autoNormal = true;

    // no current font
    textFont = null;
    textSize = 12;
    textLeading = 14;
    textAlign = LEFT;
    textMode = MODEL;

    // if this fella is associated with an applet, then clear its background.
    // if it's been created by someone else through createGraphics,
    // they have to call background() themselves, otherwise everything gets
    // a gray background (when just a transparent surface or an empty pdf
    // is what's desired).
    // this background() call is for the Java 2D and OpenGL renderers.
    if (primarySurface) {
      //System.out.println("main drawing surface bg " + getClass().getName());
      background(backgroundColor);
    }

    settingsInited = true;
    // defaultSettings() overlaps reapplySettings(), don't do both
    //reapplySettings = false;
  }


  /**
   * Re-apply current settings. Some methods, such as textFont(), require that
   * their methods be called (rather than simply setting the textFont variable)
   * because they affect the graphics context, or they require parameters from
   * the context (e.g. getting native fonts for text).
   *
   * This will only be called from an allocate(), which is only called from
   * size(), which is safely called from inside beginDraw(). And it cannot be
   * called before defaultSettings(), so we should be safe.
   */
  protected void reapplySettings() {
//    System.out.println("attempting reapplySettings()");
    if (!settingsInited) return;  // if this is the initial setup, no need to reapply

//    System.out.println("  doing reapplySettings");
//    new Exception().printStackTrace(System.out);

    colorMode(colorMode, colorModeX, colorModeY, colorModeZ);
    if (fill) {
//      PApplet.println("  fill " + PApplet.hex(fillColor));
      fill(fillColor);
    } else {
      noFill();
    }
    if (stroke) {
      stroke(strokeColor);

      // The if() statements should be handled inside the functions,
      // otherwise an actual reset/revert won't work properly.
      //if (strokeWeight != DEFAULT_STROKE_WEIGHT) {
      strokeWeight(strokeWeight);
      //}
//      if (strokeCap != DEFAULT_STROKE_CAP) {
      strokeCap(strokeCap);
//      }
//      if (strokeJoin != DEFAULT_STROKE_JOIN) {
      strokeJoin(strokeJoin);
//      }
    } else {
      noStroke();
    }
    if (tint) {
      tint(tintColor);
    } else {
      noTint();
    }
    if (smooth) {
      smooth();
    } else {
      // Don't bother setting this, cuz it'll anger P3D.
      noSmooth();
    }
    if (textFont != null) {
//      System.out.println("  textFont in reapply is " + textFont);
      // textFont() resets the leading, so save it in case it's changed
      float saveLeading = textLeading;
      textFont(textFont, textSize);
      textLeading(saveLeading);
    }
    textMode(textMode);
    textAlign(textAlign, textAlignY);
    background(backgroundColor);

    //reapplySettings = false;
  }

  // inherit from PImage
  //public void resize(int wide, int high){ }

  //////////////////////////////////////////////////////////////

  // HINTS

  /**
   * Set various hints and hacks for the renderer. This is used to handle obscure rendering features that cannot be implemented in a consistent manner across renderers. Many options will often graduate to standard features instead of hints over time.
   * <br><br>hint(ENABLE_OPENGL_4X_SMOOTH) - Enable 4x anti-aliasing for OpenGL. This can help force anti-aliasing if it has not been enabled by the user. On some graphics cards, this can also be set by the graphics driver's control panel, however not all cards make this available. This hint must be called immediately after the size() command because it resets the renderer, obliterating any settings and anything drawn (and like size(), re-running the code that came before it again).
   * <br><br>hint(DISABLE_OPENGL_2X_SMOOTH) - In Processing 1.0, Processing always enables 2x smoothing when the OpenGL renderer is used. This hint disables the default 2x smoothing and returns the smoothing behavior found in earlier releases, where smooth() and noSmooth() could be used to enable and disable smoothing, though the quality was inferior.
   * <br><br>hint(ENABLE_NATIVE_FONTS) - Use the native version fonts when they are installed, rather than the bitmapped version from a .vlw file. This is useful with the JAVA2D renderer setting, as it will improve font rendering speed. This is not enabled by default, because it can be misleading while testing because the type will look great on your machine (because you have the font installed) but lousy on others' machines if the identical font is unavailable. This option can only be set per-sketch, and must be called before any use of textFont().
   * <br><br>hint(DISABLE_DEPTH_TEST) - Disable the zbuffer, allowing you to draw on top of everything at will. When depth testing is disabled, items will be drawn to the screen sequentially, like a painting. This hint is most often used to draw in 3D, then draw in 2D on top of it (for instance, to draw GUI controls in 2D on top of a 3D interface). Starting in release 0149, this will also clear the depth buffer. Restore the default with hint(ENABLE_DEPTH_TEST), but note that with the depth buffer cleared, any 3D drawing that happens later in draw() will ignore existing shapes on the screen.
   * <br><br>hint(ENABLE_DEPTH_SORT) - Enable primitive z-sorting of triangles and lines in P3D and OPENGL. This can slow performance considerably, and the algorithm is not yet perfect. Restore the default with hint(DISABLE_DEPTH_SORT).
   * <br><br>hint(DISABLE_OPENGL_ERROR_REPORT) - Speeds up the OPENGL renderer setting by not checking for errors while running. Undo with hint(ENABLE_OPENGL_ERROR_REPORT).
   * <br><br><!--hint(ENABLE_ACCURATE_TEXTURES) - Enables better texture accuracy for the P3D renderer. This option will do a better job of dealing with textures in perspective. hint(DISABLE_ACCURATE_TEXTURES) returns to the default. This hint is not likely to last long.
   * <br/> <br/>-->As of release 0149, unhint() has been removed in favor of adding additional ENABLE/DISABLE constants to reset the default behavior. This prevents the double negatives, and also reinforces which hints can be enabled or disabled.
   *
   * @webref rendering
   * @param which name of the hint to be enabled or disabled
   *
   * @see processing.core.PGraphics
   * @see processing.core.PApplet#createGraphics(int, int, String, String)
   * @see processing.core.PApplet#size(int, int)
   */
  public void hint(int which) {
    if (which > 0) {
      hints[which] = true;
    } else {
      hints[-which] = false;
    }
  }

  public boolean hintEnabled(int which) {
    if (which > 0) {
      return hints[which];
    } else {
      return hints[-which];
    }
  }


  //////////////////////////////////////////////////////////////

  // VERTEX SHAPES

  /**
   * Start a new shape of type POLYGON
   */
  public void beginShape() {
    beginShape(POLYGON);
  }


  /**
   * Start a new shape.
   * <P>
   * <B>Differences between beginShape() and line() and point() methods.</B>
   * <P>
   * beginShape() is intended to be more flexible at the expense of being
   * a little more complicated to use. it handles more complicated shapes
   * that can consist of many connected lines (so you get joins) or lines
   * mixed with curves.
   * <P>
   * The line() and point() command are for the far more common cases
   * (particularly for our audience) that simply need to draw a line
   * or a point on the screen.
   * <P>
   * From the code side of things, line() may or may not call beginShape()
   * to do the drawing. In the beta code, they do, but in the alpha code,
   * they did not. they might be implemented one way or the other depending
   * on tradeoffs of runtime efficiency vs. implementation efficiency &mdash
   * meaning the speed that things run at vs. the speed it takes me to write
   * the code and maintain it. for beta, the latter is most important so
   * that's how things are implemented.
   */
  public void beginShape(int kind) {
    shape = kind;
  }


  /**
   * Sets whether the upcoming vertex is part of an edge.
   * Equivalent to glEdgeFlag(), for people familiar with OpenGL.
   */
  public void edge(boolean edge) {
   this.edge = edge;
  }


  /**
   * Sets the current normal vector. Only applies with 3D rendering
   * and inside a beginShape/endShape block.
   * <P/>
   * This is for drawing three dimensional shapes and surfaces,
   * allowing you to specify a vector perpendicular to the surface
   * of the shape, which determines how lighting affects it.
   * <P/>
   * For the most part, PGraphics3D will attempt to automatically
   * assign normals to shapes, but since that's imperfect,
   * this is a better option when you want more control.
   * <P/>
   * For people familiar with OpenGL, this function is basically
   * identical to glNormal3f().
   */
  public void normal(float nx, float ny, float nz) {
    normalX = nx;
    normalY = ny;
    normalZ = nz;

    // if drawing a shape and the normal hasn't been set yet,
    // then we need to set the normals for each vertex so far
    if (shape != 0) {
      if (normalMode == NORMAL_MODE_AUTO) {
        // either they set the normals, or they don't [0149]
//        for (int i = vertex_start; i < vertexCount; i++) {
//          vertices[i][NX] = normalX;
//          vertices[i][NY] = normalY;
//          vertices[i][NZ] = normalZ;
//        }
        // One normal per begin/end shape
        normalMode = NORMAL_MODE_SHAPE;

      } else if (normalMode == NORMAL_MODE_SHAPE) {
        // a separate normal for each vertex
        normalMode = NORMAL_MODE_VERTEX;
      }
    }
  }


  /**
   * Set texture mode to either to use coordinates based on the IMAGE
   * (more intuitive for new users) or NORMALIZED (better for advanced chaps)
   */
  public void textureMode(int mode) {
    this.textureMode = mode;
  }


  /**
   * Set texture image for current shape.
   * Needs to be called between @see beginShape and @see endShape
   *
   * @param image reference to a PImage object
   */
  public void texture(PImage image) {
    textureImage = image;
  }


  /**
   * Removes texture image for current shape.
   * Needs to be called between @see beginShape and @see endShape
   *
   */
  public void noTexture() {
    textureImage = null;
  }


  protected void vertexCheck() {
    if (vertexCount == vertices.length) {
      float temp[][] = new float[vertexCount << 1][VERTEX_FIELD_COUNT];
      System.arraycopy(vertices, 0, temp, 0, vertexCount);
      vertices = temp;
    }
  }


  public void vertex(float x, float y) {
    vertexCheck();
    float[] vertex = vertices[vertexCount];

    curveVertexCount = 0;

    vertex[X] = x;
    vertex[Y] = y;

    vertex[EDGE] = edge ? 1 : 0;

//    if (fill) {
//      vertex[R] = fillR;
//      vertex[G] = fillG;
//      vertex[B] = fillB;
//      vertex[A] = fillA;
//    }
    boolean textured = textureImage != null;
    if (fill || textured) {
      if (textureImage == null) {
        vertex[R] = fillR;
        vertex[G] = fillG;
        vertex[B] = fillB;
        vertex[A] = fillA;
      } else {
        if (tint) {
          vertex[R] = tintR;
          vertex[G] = tintG;
          vertex[B] = tintB;
          vertex[A] = tintA;
        } else {
          vertex[R] = 1;
          vertex[G] = 1;
          vertex[B] = 1;
          vertex[A] = 1;
        }
      }
    }

    if (stroke) {
      vertex[SR] = strokeR;
      vertex[SG] = strokeG;
      vertex[SB] = strokeB;
      vertex[SA] = strokeA;
      vertex[SW] = strokeWeight;
    }

    if (textured) {
      vertex[U] = textureU;
      vertex[V] = textureV;
    }

    if (autoNormal) {
      float norm2 = normalX * normalX + normalY * normalY + normalZ * normalZ;
      if (norm2 < EPSILON) {
        vertex[HAS_NORMAL] = 0;
      } else {
        if (Math.abs(norm2 - 1) > EPSILON) {
          // The normal vector is not normalized.
          float norm = PApplet.sqrt(norm2);
          normalX /= norm;
          normalY /= norm;
          normalZ /= norm;
        }
        vertex[HAS_NORMAL] = 1;
      }
    } else {
      vertex[HAS_NORMAL] = 1;
    }

    vertexCount++;
  }


  public void vertex(float x, float y, float z) {
    vertexCheck();
    float[] vertex = vertices[vertexCount];

    // only do this if we're using an irregular (POLYGON) shape that
    // will go through the triangulator. otherwise it'll do thinks like
    // disappear in mathematically odd ways
    // http://dev.processing.org/bugs/show_bug.cgi?id=444
    if (shape == POLYGON) {
      if (vertexCount > 0) {
        float pvertex[] = vertices[vertexCount-1];
        if ((Math.abs(pvertex[X] - x) < EPSILON) &&
            (Math.abs(pvertex[Y] - y) < EPSILON) &&
            (Math.abs(pvertex[Z] - z) < EPSILON)) {
          // this vertex is identical, don't add it,
          // because it will anger the triangulator
          return;
        }
      }
    }

    // User called vertex(), so that invalidates anything queued up for curve
    // vertices. If this is internally called by curveVertexSegment,
    // then curveVertexCount will be saved and restored.
    curveVertexCount = 0;

    vertex[X] = x;
    vertex[Y] = y;
    vertex[Z] = z;

    vertex[EDGE] = edge ? 1 : 0;

    boolean textured = textureImage != null;
    if (fill || textured) {
      if (textureImage == null) {
        vertex[R] = fillR;
        vertex[G] = fillG;
        vertex[B] = fillB;
        vertex[A] = fillA;
      } else {
        if (tint) {
          vertex[R] = tintR;
          vertex[G] = tintG;
          vertex[B] = tintB;
          vertex[A] = tintA;
        } else {
          vertex[R] = 1;
          vertex[G] = 1;
          vertex[B] = 1;
          vertex[A] = 1;
        }
      }

      vertex[AR] = ambientR;
      vertex[AG] = ambientG;
      vertex[AB] = ambientB;

      vertex[SPR] = specularR;
      vertex[SPG] = specularG;
      vertex[SPB] = specularB;
      //vertex[SPA] = specularA;

      vertex[SHINE] = shininess;

      vertex[ER] = emissiveR;
      vertex[EG] = emissiveG;
      vertex[EB] = emissiveB;
    }

    if (stroke) {
      vertex[SR] = strokeR;
      vertex[SG] = strokeG;
      vertex[SB] = strokeB;
      vertex[SA] = strokeA;
      vertex[SW] = strokeWeight;
    }

    if (textured) {
      vertex[U] = textureU;
      vertex[V] = textureV;
    }

    if (autoNormal) {
      float norm2 = normalX * normalX + normalY * normalY + normalZ * normalZ;
      if (norm2 < EPSILON) {
        vertex[HAS_NORMAL] = 0;
      } else {
        if (Math.abs(norm2 - 1) > EPSILON) {
          // The normal vector is not normalized.
          float norm = PApplet.sqrt(norm2);
          normalX /= norm;
          normalY /= norm;
          normalZ /= norm;
        }
        vertex[HAS_NORMAL] = 1;
      }
    } else {
      vertex[HAS_NORMAL] = 1;
    }

    vertex[NX] = normalX;
    vertex[NY] = normalY;
    vertex[NZ] = normalZ;

    vertex[BEEN_LIT] = 0;

    vertexCount++;
  }


  /**
   * Used by renderer subclasses or PShape to efficiently pass in already
   * formatted vertex information.
   * @param v vertex parameters, as a float array of length VERTEX_FIELD_COUNT
   */
  public void vertexFields(float[] v) {
    vertexCheck();
    curveVertexCount = 0;
    float[] vertex = vertices[vertexCount];
    System.arraycopy(v, 0, vertex, 0, VERTEX_FIELD_COUNT);
    vertexCount++;
  }


  public void vertex(float x, float y, float u, float v) {
    vertexTexture(u, v);
    vertex(x, y);
  }


  public void vertex(float x, float y, float z, float u, float v) {
    vertexTexture(u, v);
    vertex(x, y, z);
  }


  /**
   * Internal method to copy all style information for the given vertex.
   * Can be overridden by subclasses to handle only properties pertinent to
   * that renderer. (e.g. no need to copy the emissive color in P2D)
   */
//  protected void vertexStyle() {
//  }


  /**
   * Set (U, V) coords for the next vertex in the current shape.
   * This is ugly as its own function, and will (almost?) always be
   * coincident with a call to vertex. As of beta, this was moved to
   * the protected method you see here, and called from an optional
   * param of and overloaded vertex().
   * <p/>
   * The parameters depend on the current textureMode. When using
   * textureMode(IMAGE), the coordinates will be relative to the size
   * of the image texture, when used with textureMode(NORMAL),
   * they'll be in the range 0..1.
   * <p/>
   * Used by both PGraphics2D (for images) and PGraphics3D.
   */
  protected void vertexTexture(float u, float v) {
    if (textureImage == null) {
      throw new RuntimeException("You must first call texture() before " +
                                 "using u and v coordinates with vertex()");
    }
    if (textureMode == IMAGE) {
      u /= (float) textureImage.width;
      v /= (float) textureImage.height;
    }

    textureU = u;
    textureV = v;

    if (textureU < 0) textureU = 0;
    else if (textureU > 1) textureU = 1;

    if (textureV < 0) textureV = 0;
    else if (textureV > 1) textureV = 1;
  }


  /** This feature is in testing, do not use or rely upon its implementation */
  public void breakShape() {
    showWarning("This renderer cannot currently handle concave shapes, " +
                "or shapes with holes.");
  }


  public void endShape() {
    endShape(OPEN);
  }


  public void endShape(int mode) {
  }



  //////////////////////////////////////////////////////////////

  // CURVE/BEZIER VERTEX HANDLING


  protected void bezierVertexCheck() {
    if (shape == 0 || shape != POLYGON) {
      throw new RuntimeException("beginShape() or beginShape(POLYGON) " +
                                 "must be used before bezierVertex() or quadVertex()");
    }
    if (vertexCount == 0) {
      throw new RuntimeException("vertex() must be used at least once" +
                                 "before bezierVertex() or quadVertex()");
    }
  }


  public void bezierVertex(float x2, float y2,
                           float x3, float y3,
                           float x4, float y4) {
    bezierInitCheck();
    bezierVertexCheck();
    PMatrix3D draw = bezierDrawMatrix;

    float[] prev = vertices[vertexCount-1];
    float x1 = prev[X];
    float y1 = prev[Y];

    float xplot1 = draw.m10*x1 + draw.m11*x2 + draw.m12*x3 + draw.m13*x4;
    float xplot2 = draw.m20*x1 + draw.m21*x2 + draw.m22*x3 + draw.m23*x4;
    float xplot3 = draw.m30*x1 + draw.m31*x2 + draw.m32*x3 + draw.m33*x4;

    float yplot1 = draw.m10*y1 + draw.m11*y2 + draw.m12*y3 + draw.m13*y4;
    float yplot2 = draw.m20*y1 + draw.m21*y2 + draw.m22*y3 + draw.m23*y4;
    float yplot3 = draw.m30*y1 + draw.m31*y2 + draw.m32*y3 + draw.m33*y4;

    for (int j = 0; j < bezierDetail; j++) {
      x1 += xplot1; xplot1 += xplot2; xplot2 += xplot3;
      y1 += yplot1; yplot1 += yplot2; yplot2 += yplot3;
      vertex(x1, y1);
    }
  }


  public void bezierVertex(float x2, float y2, float z2,
                           float x3, float y3, float z3,
                           float x4, float y4, float z4) {
    bezierInitCheck();
    bezierVertexCheck();
    PMatrix3D draw = bezierDrawMatrix;

    float[] prev = vertices[vertexCount-1];
    float x1 = prev[X];
    float y1 = prev[Y];
    float z1 = prev[Z];

    float xplot1 = draw.m10*x1 + draw.m11*x2 + draw.m12*x3 + draw.m13*x4;
    float xplot2 = draw.m20*x1 + draw.m21*x2 + draw.m22*x3 + draw.m23*x4;
    float xplot3 = draw.m30*x1 + draw.m31*x2 + draw.m32*x3 + draw.m33*x4;

    float yplot1 = draw.m10*y1 + draw.m11*y2 + draw.m12*y3 + draw.m13*y4;
    float yplot2 = draw.m20*y1 + draw.m21*y2 + draw.m22*y3 + draw.m23*y4;
    float yplot3 = draw.m30*y1 + draw.m31*y2 + draw.m32*y3 + draw.m33*y4;

    float zplot1 = draw.m10*z1 + draw.m11*z2 + draw.m12*z3 + draw.m13*z4;
    float zplot2 = draw.m20*z1 + draw.m21*z2 + draw.m22*z3 + draw.m23*z4;
    float zplot3 = draw.m30*z1 + draw.m31*z2 + draw.m32*z3 + draw.m33*z4;

    for (int j = 0; j < bezierDetail; j++) {
      x1 += xplot1; xplot1 += xplot2; xplot2 += xplot3;
      y1 += yplot1; yplot1 += yplot2; yplot2 += yplot3;
      z1 += zplot1; zplot1 += zplot2; zplot2 += zplot3;
      vertex(x1, y1, z1);
    }
  }


  public void quadVertex(float cx, float cy,
                         float x3, float y3) {
    float[] prev = vertices[vertexCount-1];
    float x1 = prev[X];
    float y1 = prev[Y];
    
    bezierVertex(x1 + ((cx-x1)*2/3.0f), y1 + ((cy-y1)*2/3.0f),
                 x3 + ((cx-x3)*2/3.0f), y3 + ((cy-y3)*2/3.0f),
                 x3, y3);
  }


  public void quadVertex(float cx, float cy, float cz,
                         float x3, float y3, float z3) {
    float[] prev = vertices[vertexCount-1];
    float x1 = prev[X];
    float y1 = prev[Y];
    float z1 = prev[Z];
    
    bezierVertex(x1 + ((cx-x1)*2/3.0f), y1 + ((cy-y1)*2/3.0f), z1 + ((cz-z1)*2/3.0f),
                 x3 + ((cx-x3)*2/3.0f), y3 + ((cy-y3)*2/3.0f), z3 + ((cz-z3)*2/3.0f),
                 x3, y3, z3);
  }


  /**
   * Perform initialization specific to curveVertex(), and handle standard
   * error modes. Can be overridden by subclasses that need the flexibility.
   */
  protected void curveVertexCheck() {
    if (shape != POLYGON) {
      throw new RuntimeException("You must use beginShape() or " +
                                 "beginShape(POLYGON) before curveVertex()");
    }
    // to improve code init time, allocate on first use.
    if (curveVertices == null) {
      curveVertices = new float[128][3];
    }

    if (curveVertexCount == curveVertices.length) {
      // Can't use PApplet.expand() cuz it doesn't do the copy properly
      float[][] temp = new float[curveVertexCount << 1][3];
      System.arraycopy(curveVertices, 0, temp, 0, curveVertexCount);
      curveVertices = temp;
    }
    curveInitCheck();
  }


  public void curveVertex(float x, float y) {
    curveVertexCheck();
    float[] vertex = curveVertices[curveVertexCount];
    vertex[X] = x;
    vertex[Y] = y;
    curveVertexCount++;

    // draw a segment if there are enough points
    if (curveVertexCount > 3) {
      curveVertexSegment(curveVertices[curveVertexCount-4][X],
                         curveVertices[curveVertexCount-4][Y],
                         curveVertices[curveVertexCount-3][X],
                         curveVertices[curveVertexCount-3][Y],
                         curveVertices[curveVertexCount-2][X],
                         curveVertices[curveVertexCount-2][Y],
                         curveVertices[curveVertexCount-1][X],
                         curveVertices[curveVertexCount-1][Y]);
    }
  }


  public void curveVertex(float x, float y, float z) {
    curveVertexCheck();
    float[] vertex = curveVertices[curveVertexCount];
    vertex[X] = x;
    vertex[Y] = y;
    vertex[Z] = z;
    curveVertexCount++;

    // draw a segment if there are enough points
    if (curveVertexCount > 3) {
      curveVertexSegment(curveVertices[curveVertexCount-4][X],
                         curveVertices[curveVertexCount-4][Y],
                         curveVertices[curveVertexCount-4][Z],
                         curveVertices[curveVertexCount-3][X],
                         curveVertices[curveVertexCount-3][Y],
                         curveVertices[curveVertexCount-3][Z],
                         curveVertices[curveVertexCount-2][X],
                         curveVertices[curveVertexCount-2][Y],
                         curveVertices[curveVertexCount-2][Z],
                         curveVertices[curveVertexCount-1][X],
                         curveVertices[curveVertexCount-1][Y],
                         curveVertices[curveVertexCount-1][Z]);
    }
  }


  /**
   * Handle emitting a specific segment of Catmull-Rom curve. This can be
   * overridden by subclasses that need more efficient rendering options.
   */
  protected void curveVertexSegment(float x1, float y1,
                                    float x2, float y2,
                                    float x3, float y3,
                                    float x4, float y4) {
    float x0 = x2;
    float y0 = y2;

    PMatrix3D draw = curveDrawMatrix;

    float xplot1 = draw.m10*x1 + draw.m11*x2 + draw.m12*x3 + draw.m13*x4;
    float xplot2 = draw.m20*x1 + draw.m21*x2 + draw.m22*x3 + draw.m23*x4;
    float xplot3 = draw.m30*x1 + draw.m31*x2 + draw.m32*x3 + draw.m33*x4;

    float yplot1 = draw.m10*y1 + draw.m11*y2 + draw.m12*y3 + draw.m13*y4;
    float yplot2 = draw.m20*y1 + draw.m21*y2 + draw.m22*y3 + draw.m23*y4;
    float yplot3 = draw.m30*y1 + draw.m31*y2 + draw.m32*y3 + draw.m33*y4;

    // vertex() will reset splineVertexCount, so save it
    int savedCount = curveVertexCount;

    vertex(x0, y0);
    for (int j = 0; j < curveDetail; j++) {
      x0 += xplot1; xplot1 += xplot2; xplot2 += xplot3;
      y0 += yplot1; yplot1 += yplot2; yplot2 += yplot3;
      vertex(x0, y0);
    }
    curveVertexCount = savedCount;
  }


  /**
   * Handle emitting a specific segment of Catmull-Rom curve. This can be
   * overridden by subclasses that need more efficient rendering options.
   */
  protected void curveVertexSegment(float x1, float y1, float z1,
                                    float x2, float y2, float z2,
                                    float x3, float y3, float z3,
                                    float x4, float y4, float z4) {
    float x0 = x2;
    float y0 = y2;
    float z0 = z2;

    PMatrix3D draw = curveDrawMatrix;

    float xplot1 = draw.m10*x1 + draw.m11*x2 + draw.m12*x3 + draw.m13*x4;
    float xplot2 = draw.m20*x1 + draw.m21*x2 + draw.m22*x3 + draw.m23*x4;
    float xplot3 = draw.m30*x1 + draw.m31*x2 + draw.m32*x3 + draw.m33*x4;

    float yplot1 = draw.m10*y1 + draw.m11*y2 + draw.m12*y3 + draw.m13*y4;
    float yplot2 = draw.m20*y1 + draw.m21*y2 + draw.m22*y3 + draw.m23*y4;
    float yplot3 = draw.m30*y1 + draw.m31*y2 + draw.m32*y3 + draw.m33*y4;

    // vertex() will reset splineVertexCount, so save it
    int savedCount = curveVertexCount;

    float zplot1 = draw.m10*z1 + draw.m11*z2 + draw.m12*z3 + draw.m13*z4;
    float zplot2 = draw.m20*z1 + draw.m21*z2 + draw.m22*z3 + draw.m23*z4;
    float zplot3 = draw.m30*z1 + draw.m31*z2 + draw.m32*z3 + draw.m33*z4;

    vertex(x0, y0, z0);
    for (int j = 0; j < curveDetail; j++) {
      x0 += xplot1; xplot1 += xplot2; xplot2 += xplot3;
      y0 += yplot1; yplot1 += yplot2; yplot2 += yplot3;
      z0 += zplot1; zplot1 += zplot2; zplot2 += zplot3;
      vertex(x0, y0, z0);
    }
    curveVertexCount = savedCount;
  }



  //////////////////////////////////////////////////////////////

  // SIMPLE SHAPES WITH ANALOGUES IN beginShape()


  public void point(float x, float y) {
    beginShape(POINTS);
    vertex(x, y);
    endShape();
  }


  /**
   * Draws a point, a coordinate in space at the dimension of one pixel.
   * The first parameter is the horizontal value for the point, the second
   * value is the vertical value for the point, and the optional third value
   * is the depth value. Drawing this shape in 3D using the <b>z</b>
   * parameter requires the P3D or OPENGL parameter in combination with
   * size as shown in the above example.
   * <br><br>Due to what appears to be a bug in Apple's Java implementation,
   * the point() and set() methods are extremely slow in some circumstances
   * when used with the default renderer. Using P2D or P3D will fix the
   * problem. Grouping many calls to point() or set() together can also
   * help. (<a href="http://dev.processing.org/bugs/show_bug.cgi?id=1094">Bug 1094</a>)
   *
   * @webref shape:2d_primitives
   * @param x x-coordinate of the point
   * @param y y-coordinate of the point
   * @param z z-coordinate of the point
   *
   * @see PGraphics#beginShape()
   */
  public void point(float x, float y, float z) {
    beginShape(POINTS);
    vertex(x, y, z);
    endShape();
  }


  public void line(float x1, float y1, float x2, float y2) {
    beginShape(LINES);
    vertex(x1, y1);
    vertex(x2, y2);
    endShape();
  }



  /**
   * Draws a line (a direct path between two points) to the screen.
   * The version of <b>line()</b> with four parameters draws the line in 2D.
   * To color a line, use the <b>stroke()</b> function. A line cannot be
   * filled, therefore the <b>fill()</b> method will not affect the color
   * of a line. 2D lines are drawn with a width of one pixel by default,
   * but this can be changed with the <b>strokeWeight()</b> function.
   * The version with six parameters allows the line to be placed anywhere
   * within XYZ space. Drawing this shape in 3D using the <b>z</b> parameter
   * requires the P3D or OPENGL parameter in combination with size as shown
   * in the above example.
   *
   * @webref shape:2d_primitives
   * @param x1 x-coordinate of the first point
   * @param y1 y-coordinate of the first point
   * @param z1 z-coordinate of the first point
   * @param x2 x-coordinate of the second point
   * @param y2 y-coordinate of the second point
   * @param z2 z-coordinate of the second point
   *
   * @see PGraphics#strokeWeight(float)
   * @see PGraphics#strokeJoin(int)
   * @see PGraphics#strokeCap(int)
   * @see PGraphics#beginShape()
   */
  public void line(float x1, float y1, float z1,
                   float x2, float y2, float z2) {
    beginShape(LINES);
    vertex(x1, y1, z1);
    vertex(x2, y2, z2);
    endShape();
  }


  /**
   * A triangle is a plane created by connecting three points. The first two
   * arguments specify the first point, the middle two arguments specify
   * the second point, and the last two arguments specify the third point.
   *
   * @webref shape:2d_primitives
   * @param x1 x-coordinate of the first point
   * @param y1 y-coordinate of the first point
   * @param x2 x-coordinate of the second point
   * @param y2 y-coordinate of the second point
   * @param x3 x-coordinate of the third point
   * @param y3 y-coordinate of the third point
   *
   * @see PApplet#beginShape()
   */
  public void triangle(float x1, float y1, float x2, float y2,
                       float x3, float y3) {
    beginShape(TRIANGLES);
    vertex(x1, y1);
    vertex(x2, y2);
    vertex(x3, y3);
    endShape();
  }


  /**
   * A quad is a quadrilateral, a four sided polygon. It is similar to
   * a rectangle, but the angles between its edges are not constrained
   * ninety degrees. The first pair of parameters (x1,y1) sets the
   * first vertex and the subsequent pairs should proceed clockwise or
   * counter-clockwise around the defined shape.
   *
   * @webref shape:2d_primitives
   * @param x1 x-coordinate of the first corner
   * @param y1 y-coordinate of the first corner
   * @param x2 x-coordinate of the second corner
   * @param y2 y-coordinate of the second corner
   * @param x3 x-coordinate of the third corner
   * @param y3 y-coordinate of the third corner
   * @param x4 x-coordinate of the fourth corner
   * @param y4 y-coordinate of the fourth corner
   *
   */
  public void quad(float x1, float y1, float x2, float y2,
                   float x3, float y3, float x4, float y4) {
    beginShape(QUADS);
    vertex(x1, y1);
    vertex(x2, y2);
    vertex(x3, y3);
    vertex(x4, y4);
    endShape();
  }



  //////////////////////////////////////////////////////////////

  // RECT


  public void rectMode(int mode) {
    rectMode = mode;
  }


  /**
   * Draws a rectangle to the screen. A rectangle is a four-sided shape with
   * every angle at ninety degrees. The first two parameters set the location,
   * the third sets the width, and the fourth sets the height. The origin is
   * changed with the <b>rectMode()</b> function.
   *
   * @webref shape:2d_primitives
   * @param a x-coordinate of the rectangle
   * @param b y-coordinate of the rectangle
   * @param c width of the rectangle
   * @param d height of the rectangle
   *
   * @see PGraphics#rectMode(int)
   * @see PGraphics#quad(float, float, float, float, float, float, float, float)
   */
  public void rect(float a, float b, float c, float d) {
    float hradius, vradius;
    switch (rectMode) {
    case CORNERS:
      break;
    case CORNER:
      c += a; d += b;
      break;
    case RADIUS:
      hradius = c;
      vradius = d;
      c = a + hradius;
      d = b + vradius;
      a -= hradius;
      b -= vradius;
      break;
    case CENTER:
      hradius = c / 2.0f;
      vradius = d / 2.0f;
      c = a + hradius;
      d = b + vradius;
      a -= hradius;
      b -= vradius;
    }

    if (a > c) {
      float temp = a; a = c; c = temp;
    }

    if (b > d) {
      float temp = b; b = d; d = temp;
    }

    rectImpl(a, b, c, d);
  }


  protected void rectImpl(float x1, float y1, float x2, float y2) {
    quad(x1, y1,  x2, y1,  x2, y2,  x1, y2);
  }


  // Still need to do a lot of work here to make it behave across renderers
  // (e.g. not all renderers use the vertices array)
  // Also seems to be some issues on quality here (too dense)
  // http://code.google.com/p/processing/issues/detail?id=265
  private void quadraticVertex(float cpx, float cpy, float x, float y) {
    float[] prev = vertices[vertexCount - 1];
    float prevX = prev[X];
    float prevY = prev[Y];
    float cp1x = prevX + 2.0f/3.0f*(cpx - prevX);
    float cp1y = prevY + 2.0f/3.0f*(cpy - prevY);
    float cp2x = cp1x + (x - prevX)/3.0f;
    float cp2y = cp1y + (y - prevY)/3.0f;
    bezierVertex(cp1x, cp1y, cp2x, cp2y, x, y);
  }


  public void rect(float a, float b, float c, float d, float hr, float vr) {
    float hradius, vradius;
    switch (rectMode) {
    case CORNERS:
      break;
    case CORNER:
      c += a; d += b;
      break;
    case RADIUS:
      hradius = c;
      vradius = d;
      c = a + hradius;
      d = b + vradius;
      a -= hradius;
      b -= vradius;
      break;
    case CENTER:
      hradius = c / 2.0f;
      vradius = d / 2.0f;
      c = a + hradius;
      d = b + vradius;
      a -= hradius;
      b -= vradius;
    }

    if (a > c) {
      float temp = a; a = c; c = temp;
    }

    if (b > d) {
      float temp = b; b = d; d = temp;
    }

    rectImpl(a, b, c, d, hr, vr);
  }


  protected void rectImpl(float x1, float y1, float x2, float y2, float hr, float vr) {
    beginShape();
//    vertex(x1+hr, y1);
    vertex(x2-hr, y1);
    quadraticVertex(x2, y1, x2, y1+vr);
    vertex(x2, y2-vr);
    quadraticVertex(x2, y2, x2-hr, y2);
    vertex(x1+hr, y2);
    quadraticVertex(x1, y2, x1, y2-vr);
    vertex(x1, y1+vr);
    quadraticVertex(x1, y1, x1+hr, y1);
//    endShape();
    endShape(CLOSE);
  }


  public void rect(float a, float b, float c, float d,
                   float tl, float tr, float bl, float br) {
    float hradius, vradius;
    switch (rectMode) {
    case CORNERS:
      break;
    case CORNER:
      c += a; d += b;
      break;
    case RADIUS:
      hradius = c;
      vradius = d;
      c = a + hradius;
      d = b + vradius;
      a -= hradius;
      b -= vradius;
      break;
    case CENTER:
      hradius = c / 2.0f;
      vradius = d / 2.0f;
      c = a + hradius;
      d = b + vradius;
      a -= hradius;
      b -= vradius;
    }

    if (a > c) {
      float temp = a; a = c; c = temp;
    }

    if (b > d) {
      float temp = b; b = d; d = temp;
    }

    rectImpl(a, b, c, d, tl, tr, bl, br);
  }


  protected void rectImpl(float x1, float y1, float x2, float y2,
                          float tl, float tr, float bl, float br) {
    beginShape();
//    vertex(x1+tl, y1);
    if (tr != 0) {
      vertex(x2-tr, y1);
      quadraticVertex(x2, y1, x2, y1+tr);
    } else {
      vertex(x2, y1);
    }
    if (br != 0) {
      vertex(x2, y2-br);
      quadraticVertex(x2, y2, x2-br, y2);
    } else {
      vertex(x2, y2);
    }
    if (bl != 0) {
      vertex(x1+bl, y2);
      quadraticVertex(x1, y2, x1, y2-bl);
    } else {
      vertex(x1, y2);
    }
    if (tl != 0) {
      vertex(x1, y1+tl);
      quadraticVertex(x1, y1, x1+tl, y1);
    } else {
      vertex(x1, y1);
    }
//    endShape();
    endShape(CLOSE);
  }



  //////////////////////////////////////////////////////////////

  // ELLIPSE AND ARC


  /**
   * The origin of the ellipse is modified by the <b>ellipseMode()</b>
   * function. The default configuration is <b>ellipseMode(CENTER)</b>,
   * which specifies the location of the ellipse as the center of the shape.
   * The RADIUS mode is the same, but the width and height parameters to
   * <b>ellipse()</b> specify the radius of the ellipse, rather than the
   * diameter. The CORNER mode draws the shape from the upper-left corner
   * of its bounding box. The CORNERS mode uses the four parameters to
   * <b>ellipse()</b> to set two opposing corners of the ellipse's bounding
   * box. The parameter must be written in "ALL CAPS" because Processing
   * syntax is case sensitive.
   *
   * @webref shape:attributes
   *
   * @param mode        Either CENTER, RADIUS, CORNER, or CORNERS.
   * @see PApplet#ellipse(float, float, float, float)
   */
  public void ellipseMode(int mode) {
    ellipseMode = mode;
  }


  /**
   * Draws an ellipse (oval) in the display window. An ellipse with an equal
   * <b>width</b> and <b>height</b> is a circle. The first two parameters set
   * the location, the third sets the width, and the fourth sets the height.
   * The origin may be changed with the <b>ellipseMode()</b> function.
   *
   * @webref shape:2d_primitives
   * @param a x-coordinate of the ellipse
   * @param b y-coordinate of the ellipse
   * @param c width of the ellipse
   * @param d height of the ellipse
   *
   * @see PApplet#ellipseMode(int)
   */
  public void ellipse(float a, float b, float c, float d) {
    float x = a;
    float y = b;
    float w = c;
    float h = d;

    if (ellipseMode == CORNERS) {
      w = c - a;
      h = d - b;

    } else if (ellipseMode == RADIUS) {
      x = a - c;
      y = b - d;
      w = c * 2;
      h = d * 2;

    } else if (ellipseMode == DIAMETER) {
      x = a - c/2f;
      y = b - d/2f;
    }

    if (w < 0) {  // undo negative width
      x += w;
      w = -w;
    }

    if (h < 0) {  // undo negative height
      y += h;
      h = -h;
    }

    ellipseImpl(x, y, w, h);
  }


  protected void ellipseImpl(float x, float y, float w, float h) {
  }


  /**
   * Draws an arc in the display window.
   * Arcs are drawn along the outer edge of an ellipse defined by the
   * <b>x</b>, <b>y</b>, <b>width</b> and <b>height</b> parameters.
   * The origin or the arc's ellipse may be changed with the
   * <b>ellipseMode()</b> function.
   * The <b>start</b> and <b>stop</b> parameters specify the angles
   * at which to draw the arc.
   *
   * @webref shape:2d_primitives
   * @param a x-coordinate of the arc's ellipse
   * @param b y-coordinate of the arc's ellipse
   * @param c width of the arc's ellipse
   * @param d height of the arc's ellipse
   * @param start angle to start the arc, specified in radians
   * @param stop angle to stop the arc, specified in radians
   *
   * @see PGraphics#ellipseMode(int)
   * @see PGraphics#ellipse(float, float, float, float)
   */
  public void arc(float a, float b, float c, float d,
                  float start, float stop) {
    float x = a;
    float y = b;
    float w = c;
    float h = d;

    if (ellipseMode == CORNERS) {
      w = c - a;
      h = d - b;

    } else if (ellipseMode == RADIUS) {
      x = a - c;
      y = b - d;
      w = c * 2;
      h = d * 2;

    } else if (ellipseMode == CENTER) {
      x = a - c/2f;
      y = b - d/2f;
    }

    // make sure this loop will exit before starting while
    if (Float.isInfinite(start) || Float.isInfinite(stop)) return;
//    while (stop < start) stop += TWO_PI;
    if (stop < start) return;  // why bother

    // make sure that we're starting at a useful point
    while (start < 0) {
      start += TWO_PI;
      stop += TWO_PI;
    }

    if (stop - start > TWO_PI) {
      start = 0;
      stop = TWO_PI;
    }

    arcImpl(x, y, w, h, start, stop);
  }


  /**
   * Start and stop are in radians, converted by the parent function.
   * Note that the radians can be greater (or less) than TWO_PI.
   * This is so that an arc can be drawn that crosses zero mark,
   * and the user will still collect $200.
   */
  protected void arcImpl(float x, float y, float w, float h,
                         float start, float stop) {
  }



  //////////////////////////////////////////////////////////////

  // BOX


  /**
   * @param size dimension of the box in all dimensions, creates a cube
   */
  public void box(float size) {
    box(size, size, size);
  }


  /**
   * A box is an extruded rectangle. A box with equal dimension
   * on all sides is a cube.
   *
   * @webref shape:3d_primitives
   * @param w dimension of the box in the x-dimension
   * @param h dimension of the box in the y-dimension
   * @param d dimension of the box in the z-dimension
   *
   * @see PApplet#sphere(float)
   */
  public void box(float w, float h, float d) {
    float x1 = -w/2f; float x2 = w/2f;
    float y1 = -h/2f; float y2 = h/2f;
    float z1 = -d/2f; float z2 = d/2f;

    // TODO not the least bit efficient, it even redraws lines
    // along the vertices. ugly ugly ugly!

    beginShape(QUADS);

    // front
    normal(0, 0, 1);
    vertex(x1, y1, z1);
    vertex(x2, y1, z1);
    vertex(x2, y2, z1);
    vertex(x1, y2, z1);

    // right
    normal(1, 0, 0);
    vertex(x2, y1, z1);
    vertex(x2, y1, z2);
    vertex(x2, y2, z2);
    vertex(x2, y2, z1);

    // back
    normal(0, 0, -1);
    vertex(x2, y1, z2);
    vertex(x1, y1, z2);
    vertex(x1, y2, z2);
    vertex(x2, y2, z2);

    // left
    normal(-1, 0, 0);
    vertex(x1, y1, z2);
    vertex(x1, y1, z1);
    vertex(x1, y2, z1);
    vertex(x1, y2, z2);

    // top
    normal(0, 1, 0);
    vertex(x1, y1, z2);
    vertex(x2, y1, z2);
    vertex(x2, y1, z1);
    vertex(x1, y1, z1);

    // bottom
    normal(0, -1, 0);
    vertex(x1, y2, z1);
    vertex(x2, y2, z1);
    vertex(x2, y2, z2);
    vertex(x1, y2, z2);

    endShape();
  }



  //////////////////////////////////////////////////////////////

  // SPHERE


  /**
   * @param res number of segments (minimum 3) used per full circle revolution
   */
  public void sphereDetail(int res) {
    sphereDetail(res, res);
  }


  /**
   * Controls the detail used to render a sphere by adjusting the number of
   * vertices of the sphere mesh. The default resolution is 30, which creates
   * a fairly detailed sphere definition with vertices every 360/30 = 12
   * degrees. If you're going to render a great number of spheres per frame,
   * it is advised to reduce the level of detail using this function.
   * The setting stays active until <b>sphereDetail()</b> is called again with
   * a new parameter and so should <i>not</i> be called prior to every
   * <b>sphere()</b> statement, unless you wish to render spheres with
   * different settings, e.g. using less detail for smaller spheres or ones
   * further away from the camera. To control the detail of the horizontal
   * and vertical resolution independently, use the version of the functions
   * with two parameters.
   *
   * =advanced
   * Code for sphereDetail() submitted by toxi [031031].
   * Code for enhanced u/v version from davbol [080801].
   *
   * @webref shape:3d_primitives
   * @param ures number of segments used horizontally (longitudinally)
   *        per full circle revolution
   * @param vres number of segments used vertically (latitudinally)
   *        from top to bottom
   *
   * @see PGraphics#sphere(float)
   */
  /**
   * Set the detail level for approximating a sphere. The ures and vres params
   * control the horizontal and vertical resolution.
   *
   */
  public void sphereDetail(int ures, int vres) {
    if (ures < 3) ures = 3; // force a minimum res
    if (vres < 2) vres = 2; // force a minimum res
    if ((ures == sphereDetailU) && (vres == sphereDetailV)) return;

    float delta = (float)SINCOS_LENGTH/ures;
    float[] cx = new float[ures];
    float[] cz = new float[ures];
    // calc unit circle in XZ plane
    for (int i = 0; i < ures; i++) {
      cx[i] = cosLUT[(int) (i*delta) % SINCOS_LENGTH];
      cz[i] = sinLUT[(int) (i*delta) % SINCOS_LENGTH];
    }
    // computing vertexlist
    // vertexlist starts at south pole
    int vertCount = ures * (vres-1) + 2;
    int currVert = 0;

    // re-init arrays to store vertices
    sphereX = new float[vertCount];
    sphereY = new float[vertCount];
    sphereZ = new float[vertCount];

    float angle_step = (SINCOS_LENGTH*0.5f)/vres;
    float angle = angle_step;

    // step along Y axis
    for (int i = 1; i < vres; i++) {
      float curradius = sinLUT[(int) angle % SINCOS_LENGTH];
      float currY = -cosLUT[(int) angle % SINCOS_LENGTH];
      for (int j = 0; j < ures; j++) {
        sphereX[currVert] = cx[j] * curradius;
        sphereY[currVert] = currY;
        sphereZ[currVert++] = cz[j] * curradius;
      }
      angle += angle_step;
    }
    sphereDetailU = ures;
    sphereDetailV = vres;
  }


  /**
   * Draw a sphere with radius r centered at coordinate 0, 0, 0.
   * A sphere is a hollow ball made from tessellated triangles.
   * =advanced
   * <P>
   * Implementation notes:
   * <P>
   * cache all the points of the sphere in a static array
   * top and bottom are just a bunch of triangles that land
   * in the center point
   * <P>
   * sphere is a series of concentric circles who radii vary
   * along the shape, based on, er.. cos or something
   * <PRE>
   * [toxi 031031] new sphere code. removed all multiplies with
   * radius, as scale() will take care of that anyway
   *
   * [toxi 031223] updated sphere code (removed modulos)
   * and introduced sphereAt(x,y,z,r)
   * to avoid additional translate()'s on the user/sketch side
   *
   * [davbol 080801] now using separate sphereDetailU/V
   * </PRE>
   *
   * @webref shape:3d_primitives
   * @param r the radius of the sphere
   */
  public void sphere(float r) {
    if ((sphereDetailU < 3) || (sphereDetailV < 2)) {
      sphereDetail(30);
    }

    edge(false);

    // 1st ring from south pole
    beginShape(TRIANGLE_STRIP);
    for (int i = 0; i < sphereDetailU; i++) {
      normal(0, -1, 0);
      vertex(0, -r, 0);
      normal(sphereX[i], sphereY[i], sphereZ[i]);
      vertex(r * sphereX[i], r *sphereY[i], r * sphereZ[i]);
    }
    normal(0, -1, 0);
    vertex(0, -1, 0);
    normal(sphereX[0], sphereY[0], sphereZ[0]);
    vertex(r * sphereX[0], r * sphereY[0], r * sphereZ[0]);
    endShape();

    int v1,v11,v2;

    // middle rings
    int voff = 0;
    for (int i = 2; i < sphereDetailV; i++) {
      v1 = v11 = voff;
      voff += sphereDetailU;
      v2 = voff;
      beginShape(TRIANGLE_STRIP);
      for (int j = 0; j < sphereDetailU; j++) {
        normal(sphereX[v1], sphereY[v1], sphereZ[v1]);
        vertex(r * sphereX[v1], r * sphereY[v1], r * sphereZ[v1++]);
        normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
        vertex(r * sphereX[v2], r * sphereY[v2], r * sphereZ[v2++]);
      }
      // close each ring
      v1 = v11;
      v2 = voff;
      normal(sphereX[v1], sphereY[v1], sphereZ[v1]);
      vertex(r * sphereX[v1], r * sphereY[v1], sphereZ[v1]);
      normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
      vertex(r * sphereX[v2], r * sphereY[v2], r * sphereZ[v2]);
      endShape();
    }

    // add the northern cap
    beginShape(TRIANGLE_STRIP);
    for (int i = 0; i < sphereDetailU; i++) {
      v2 = voff + i;
      normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
      vertex(r * sphereX[v2], r * sphereY[v2], r * sphereZ[v2]);
      normal(0, 1, 0);
      vertex(0, r, 0);
    }
    normal(sphereX[voff], sphereY[voff], sphereZ[voff]);
    vertex(r * sphereX[voff], r * sphereY[voff], r * sphereZ[voff]);
    normal(0, 1, 0);
    vertex(0, r, 0);
    endShape();

    edge(true);
  }



  //////////////////////////////////////////////////////////////

  // BEZIER

  /**
   * Evaluates the Bezier at point t for points a, b, c, d. The parameter t varies between 0 and 1, a and d are points on the curve, and b and c are the control points. This can be done once with the x coordinates and a second time with the y coordinates to get the location of a bezier curve at t.
   */

  /**
   * Evalutes quadratic bezier at point t for points a, b, c, d.
   * The parameter t varies between 0 and 1. The a and d parameters are the
   * on-curve points, b and c are the control points. To make a two-dimensional
   * curve, call this function once with the x coordinates and a second time
   * with the y coordinates to get the location of a bezier curve at t.
   *
   * =advanced
   * For instance, to convert the following example:<PRE>
   * stroke(255, 102, 0);
   * line(85, 20, 10, 10);
   * line(90, 90, 15, 80);
   * stroke(0, 0, 0);
   * bezier(85, 20, 10, 10, 90, 90, 15, 80);
   *
   * // draw it in gray, using 10 steps instead of the default 20
   * // this is a slower way to do it, but useful if you need
   * // to do things with the coordinates at each step
   * stroke(128);
   * beginShape(LINE_STRIP);
   * for (int i = 0; i <= 10; i++) {
   *   float t = i / 10.0f;
   *   float x = bezierPoint(85, 10, 90, 15, t);
   *   float y = bezierPoint(20, 10, 90, 80, t);
   *   vertex(x, y);
   * }
   * endShape();</PRE>
   *
   * @webref shape:curves
   * @param a coordinate of first point on the curve
   * @param b coordinate of first control point
   * @param c coordinate of second control point
   * @param d coordinate of second point on the curve
   * @param t value between 0 and 1
   *
   * @see PGraphics#bezier(float, float, float, float, float, float, float, float, float, float, float, float)
   * @see PGraphics#bezierVertex(float, float, float, float, float, float)
   * @see PGraphics#curvePoint(float, float, float, float, float)
   */
  public float bezierPoint(float a, float b, float c, float d, float t) {
    float t1 = 1.0f - t;
    return a*t1*t1*t1 + 3*b*t*t1*t1 + 3*c*t*t*t1 + d*t*t*t;
  }


  /**
   * Calculates the tangent of a point on a Bezier curve. There is a good
   * definition of "tangent" at Wikipedia: <a href="http://en.wikipedia.org/wiki/Tangent" target="new">http://en.wikipedia.org/wiki/Tangent</a>
   *
   * =advanced
   * Code submitted by Dave Bollinger (davol) for release 0136.
   *
   * @webref shape:curves
   * @param a coordinate of first point on the curve
   * @param b coordinate of first control point
   * @param c coordinate of second control point
   * @param d coordinate of second point on the curve
   * @param t value between 0 and 1
   *
   * @see PGraphics#bezier(float, float, float, float, float, float, float, float, float, float, float, float)
   * @see PGraphics#bezierVertex(float, float, float, float, float, float)
   * @see PGraphics#curvePoint(float, float, float, float, float)
   */
  public float bezierTangent(float a, float b, float c, float d, float t) {
    return (3*t*t * (-a+3*b-3*c+d) +
            6*t * (a-2*b+c) +
            3 * (-a+b));
  }


  protected void bezierInitCheck() {
    if (!bezierInited) {
      bezierInit();
    }
  }


  protected void bezierInit() {
    // overkill to be broken out, but better parity with the curve stuff below
    bezierDetail(bezierDetail);
    bezierInited = true;
  }


  /**
   * Sets the resolution at which Beziers display. The default value is 20. This function is only useful when using the P3D or OPENGL renderer as the default (JAVA2D) renderer does not use this information.
   *
   * @webref shape:curves
   * @param detail resolution of the curves
   *
   * @see PApplet#curve(float, float, float, float, float, float, float, float, float, float, float, float)
   * @see PApplet#curveVertex(float, float)
   * @see PApplet#curveTightness(float)
   */
  public void bezierDetail(int detail) {
    bezierDetail = detail;

    if (bezierDrawMatrix == null) {
      bezierDrawMatrix = new PMatrix3D();
    }

    // setup matrix for forward differencing to speed up drawing
    splineForward(detail, bezierDrawMatrix);

    // multiply the basis and forward diff matrices together
    // saves much time since this needn't be done for each curve
    //mult_spline_matrix(bezierForwardMatrix, bezier_basis, bezierDrawMatrix, 4);
    //bezierDrawMatrix.set(bezierForwardMatrix);
    bezierDrawMatrix.apply(bezierBasisMatrix);
  }


  /**
   * Draws a Bezier curve on the screen. These curves are defined by a series
   * of anchor and control points. The first two parameters specify the first
   * anchor point and the last two parameters specify the other anchor point.
   * The middle parameters specify the control points which define the shape
   * of the curve. Bezier curves were developed by French engineer Pierre
   * Bezier. Using the 3D version of requires rendering with P3D or OPENGL
   * (see the Environment reference for more information).
   *
   * =advanced
   * Draw a cubic bezier curve. The first and last points are
   * the on-curve points. The middle two are the 'control' points,
   * or 'handles' in an application like Illustrator.
   * <P>
   * Identical to typing:
   * <PRE>beginShape();
   * vertex(x1, y1);
   * bezierVertex(x2, y2, x3, y3, x4, y4);
   * endShape();
   * </PRE>
   * In Postscript-speak, this would be:
   * <PRE>moveto(x1, y1);
   * curveto(x2, y2, x3, y3, x4, y4);</PRE>
   * If you were to try and continue that curve like so:
   * <PRE>curveto(x5, y5, x6, y6, x7, y7);</PRE>
   * This would be done in processing by adding these statements:
   * <PRE>bezierVertex(x5, y5, x6, y6, x7, y7)
   * </PRE>
   * To draw a quadratic (instead of cubic) curve,
   * use the control point twice by doubling it:
   * <PRE>bezier(x1, y1, cx, cy, cx, cy, x2, y2);</PRE>
   *
   * @webref shape:curves
   * @param x1 coordinates for the first anchor point
   * @param y1 coordinates for the first anchor point
   * @param z1 coordinates for the first anchor point
   * @param x2 coordinates for the first control point
   * @param y2 coordinates for the first control point
   * @param z2 coordinates for the first control point
   * @param x3 coordinates for the second control point
   * @param y3 coordinates for the second control point
   * @param z3 coordinates for the second control point
   * @param x4 coordinates for the second anchor point
   * @param y4 coordinates for the second anchor point
   * @param z4 coordinates for the second anchor point
   *
   * @see PGraphics#bezierVertex(float, float, float, float, float, float)
   * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
   */
  public void bezier(float x1, float y1,
                     float x2, float y2,
                     float x3, float y3,
                     float x4, float y4) {
    beginShape();
    vertex(x1, y1);
    bezierVertex(x2, y2, x3, y3, x4, y4);
    endShape();
  }


  public void bezier(float x1, float y1, float z1,
                     float x2, float y2, float z2,
                     float x3, float y3, float z3,
                     float x4, float y4, float z4) {
    beginShape();
    vertex(x1, y1, z1);
    bezierVertex(x2, y2, z2,
                 x3, y3, z3,
                 x4, y4, z4);
    endShape();
  }



  //////////////////////////////////////////////////////////////

  // CATMULL-ROM CURVE


  /**
   * Evalutes the Catmull-Rom curve at point t for points a, b, c, d. The
   * parameter t varies between 0 and 1, a and d are points on the curve,
   * and b and c are the control points. This can be done once with the x
   * coordinates and a second time with the y coordinates to get the
   * location of a curve at t.
   *
   * @webref shape:curves
   * @param a coordinate of first point on the curve
   * @param b coordinate of second point on the curve
   * @param c coordinate of third point on the curve
   * @param d coordinate of fourth point on the curve
   * @param t value between 0 and 1
   *
   * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
   * @see PGraphics#curveVertex(float, float)
   * @see PGraphics#bezierPoint(float, float, float, float, float)
   */
  public float curvePoint(float a, float b, float c, float d, float t) {
    curveInitCheck();

    float tt = t * t;
    float ttt = t * tt;
    PMatrix3D cb = curveBasisMatrix;

    // not optimized (and probably need not be)
    return (a * (ttt*cb.m00 + tt*cb.m10 + t*cb.m20 + cb.m30) +
            b * (ttt*cb.m01 + tt*cb.m11 + t*cb.m21 + cb.m31) +
            c * (ttt*cb.m02 + tt*cb.m12 + t*cb.m22 + cb.m32) +
            d * (ttt*cb.m03 + tt*cb.m13 + t*cb.m23 + cb.m33));
  }


  /**
   * Calculates the tangent of a point on a Catmull-Rom curve. There is a good definition of "tangent" at Wikipedia: <a href="http://en.wikipedia.org/wiki/Tangent" target="new">http://en.wikipedia.org/wiki/Tangent</a>.
   *
   * =advanced
   * Code thanks to Dave Bollinger (Bug #715)
   *
   * @webref shape:curves
   * @param a coordinate of first point on the curve
   * @param b coordinate of first control point
   * @param c coordinate of second control point
   * @param d coordinate of second point on the curve
   * @param t value between 0 and 1
   *
   * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
   * @see PGraphics#curveVertex(float, float)
   * @see PGraphics#curvePoint(float, float, float, float, float)
   * @see PGraphics#bezierTangent(float, float, float, float, float)
   */
  public float curveTangent(float a, float b, float c, float d, float t) {
    curveInitCheck();

    float tt3 = t * t * 3;
    float t2 = t * 2;
    PMatrix3D cb = curveBasisMatrix;

    // not optimized (and probably need not be)
    return (a * (tt3*cb.m00 + t2*cb.m10 + cb.m20) +
            b * (tt3*cb.m01 + t2*cb.m11 + cb.m21) +
            c * (tt3*cb.m02 + t2*cb.m12 + cb.m22) +
            d * (tt3*cb.m03 + t2*cb.m13 + cb.m23) );
  }


  /**
   * Sets the resolution at which curves display. The default value is 20.
   * This function is only useful when using the P3D or OPENGL renderer as
   * the default (JAVA2D) renderer does not use this information.
   *
   * @webref shape:curves
   * @param detail resolution of the curves
   *
   * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
   * @see PGraphics#curveVertex(float, float)
   * @see PGraphics#curveTightness(float)
   */
  public void curveDetail(int detail) {
    curveDetail = detail;
    curveInit();
  }


  /**
   * Modifies the quality of forms created with <b>curve()</b> and
   *<b>curveVertex()</b>. The parameter <b>squishy</b> determines how the
   * curve fits to the vertex points. The value 0.0 is the default value for
   * <b>squishy</b> (this value defines the curves to be Catmull-Rom splines)
   * and the value 1.0 connects all the points with straight lines.
   * Values within the range -5.0 and 5.0 will deform the curves but
   * will leave them recognizable and as values increase in magnitude,
   * they will continue to deform.
   *
   * @webref shape:curves
   * @param tightness amount of deformation from the original vertices
   *
   * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
   * @see PGraphics#curveVertex(float, float)
   *
   */
  public void curveTightness(float tightness) {
    curveTightness = tightness;
    curveInit();
  }


  protected void curveInitCheck() {
    if (!curveInited) {
      curveInit();
    }
  }


  /**
   * Set the number of segments to use when drawing a Catmull-Rom
   * curve, and setting the s parameter, which defines how tightly
   * the curve fits to each vertex. Catmull-Rom curves are actually
   * a subset of this curve type where the s is set to zero.
   * <P>
   * (This function is not optimized, since it's not expected to
   * be called all that often. there are many juicy and obvious
   * opimizations in here, but it's probably better to keep the
   * code more readable)
   */
  protected void curveInit() {
    // allocate only if/when used to save startup time
    if (curveDrawMatrix == null) {
      curveBasisMatrix = new PMatrix3D();
      curveDrawMatrix = new PMatrix3D();
      curveInited = true;
    }

    float s = curveTightness;
    curveBasisMatrix.set((s-1)/2f, (s+3)/2f,  (-3-s)/2f, (1-s)/2f,
                         (1-s),    (-5-s)/2f, (s+2),     (s-1)/2f,
                         (s-1)/2f, 0,         (1-s)/2f,  0,
                         0,        1,         0,         0);

    //setup_spline_forward(segments, curveForwardMatrix);
    splineForward(curveDetail, curveDrawMatrix);

    if (bezierBasisInverse == null) {
      bezierBasisInverse = bezierBasisMatrix.get();
      bezierBasisInverse.invert();
      curveToBezierMatrix = new PMatrix3D();
    }

    // TODO only needed for PGraphicsJava2D? if so, move it there
    // actually, it's generally useful for other renderers, so keep it
    // or hide the implementation elsewhere.
    curveToBezierMatrix.set(curveBasisMatrix);
    curveToBezierMatrix.preApply(bezierBasisInverse);

    // multiply the basis and forward diff matrices together
    // saves much time since this needn't be done for each curve
    curveDrawMatrix.apply(curveBasisMatrix);
  }


  /**
   * Draws a curved line on the screen. The first and second parameters
   * specify the beginning control point and the last two parameters specify
   * the ending control point. The middle parameters specify the start and
   * stop of the curve. Longer curves can be created by putting a series of
   * <b>curve()</b> functions together or using <b>curveVertex()</b>.
   * An additional function called <b>curveTightness()</b> provides control
   * for the visual quality of the curve. The <b>curve()</b> function is an
   * implementation of Catmull-Rom splines. Using the 3D version of requires
   * rendering with P3D or OPENGL (see the Environment reference for more
   * information).
   *
   * =advanced
   * As of revision 0070, this function no longer doubles the first
   * and last points. The curves are a bit more boring, but it's more
   * mathematically correct, and properly mirrored in curvePoint().
   * <P>
   * Identical to typing out:<PRE>
   * beginShape();
   * curveVertex(x1, y1);
   * curveVertex(x2, y2);
   * curveVertex(x3, y3);
   * curveVertex(x4, y4);
   * endShape();
   * </PRE>
   *
   * @webref shape:curves
   * @param x1 coordinates for the beginning control point
   * @param y1 coordinates for the beginning control point
   * @param z1 coordinates for the beginning control point
   * @param x2 coordinates for the first point
   * @param y2 coordinates for the first point
   * @param z2 coordinates for the first point
   * @param x3 coordinates for the second point
   * @param y3 coordinates for the second point
   * @param z3 coordinates for the second point
   * @param x4 coordinates for the ending control point
   * @param y4 coordinates for the ending control point
   * @param z4 coordinates for the ending control point
   *
   * @see PGraphics#curveVertex(float, float)
   * @see PGraphics#curveTightness(float)
   * @see PGraphics#bezier(float, float, float, float, float, float, float, float, float, float, float, float)
   */
  public void curve(float x1, float y1,
                    float x2, float y2,
                    float x3, float y3,
                    float x4, float y4) {
    beginShape();
    curveVertex(x1, y1);
    curveVertex(x2, y2);
    curveVertex(x3, y3);
    curveVertex(x4, y4);
    endShape();
  }


  public void curve(float x1, float y1, float z1,
                    float x2, float y2, float z2,
                    float x3, float y3, float z3,
                    float x4, float y4, float z4) {
    beginShape();
    curveVertex(x1, y1, z1);
    curveVertex(x2, y2, z2);
    curveVertex(x3, y3, z3);
    curveVertex(x4, y4, z4);
    endShape();
  }



  //////////////////////////////////////////////////////////////

  // SPLINE UTILITY FUNCTIONS (used by both Bezier and Catmull-Rom)


  /**
   * Setup forward-differencing matrix to be used for speedy
   * curve rendering. It's based on using a specific number
   * of curve segments and just doing incremental adds for each
   * vertex of the segment, rather than running the mathematically
   * expensive cubic equation.
   * @param segments number of curve segments to use when drawing
   * @param matrix target object for the new matrix
   */
  protected void splineForward(int segments, PMatrix3D matrix) {
    float f  = 1.0f / segments;
    float ff = f * f;
    float fff = ff * f;

    matrix.set(0,     0,    0, 1,
               fff,   ff,   f, 0,
               6*fff, 2*ff, 0, 0,
               6*fff, 0,    0, 0);
  }



  //////////////////////////////////////////////////////////////

  // SMOOTHING


  /**
   * If true in PImage, use bilinear interpolation for copy()
   * operations. When inherited by PGraphics, also controls shapes.
   */
  public void smooth() {
    smooth = true;
  }


  /**
   * Disable smoothing. See smooth().
   */
  public void noSmooth() {
    smooth = false;
  }



  //////////////////////////////////////////////////////////////

  // IMAGE


  /**
   * Modifies the location from which images draw. The default mode is
   * <b>imageMode(CORNER)</b>, which specifies the location to be the
   * upper-left corner and uses the fourth and fifth parameters of
   * <b>image()</b> to set the image's width and height. The syntax
   * <b>imageMode(CORNERS)</b> uses the second and third parameters of
   * <b>image()</b> to set the location of one corner of the image and
   * uses the fourth and fifth parameters to set the opposite corner.
   * Use <b>imageMode(CENTER)</b> to draw images centered at the given
   * x and y position.
   * <br><br>The parameter to <b>imageMode()</b> must be written in
   * ALL CAPS because Processing syntax is case sensitive.
   *
   * @webref image:loading_displaying
   * @param mode Either CORNER, CORNERS, or CENTER
   *
   * @see processing.core.PApplet#loadImage(String, String)
   * @see processing.core.PImage
   * @see processing.core.PApplet#image(PImage, float, float, float, float)
   * @see processing.core.PGraphics#background(float, float, float, float)
   */
  public void imageMode(int mode) {
    if ((mode == CORNER) || (mode == CORNERS) || (mode == CENTER)) {
      imageMode = mode;
    } else {
      String msg =
        "imageMode() only works with CORNER, CORNERS, or CENTER";
      throw new RuntimeException(msg);
    }
  }


  public void image(PImage image, float x, float y) {
    // Starting in release 0144, image errors are simply ignored.
    // loadImageAsync() sets width and height to -1 when loading fails.
    if (image.width == -1 || image.height == -1) return;

    if (imageMode == CORNER || imageMode == CORNERS) {
      imageImpl(image,
                x, y, x+image.width, y+image.height,
                0, 0, image.width, image.height);

    } else if (imageMode == CENTER) {
      float x1 = x - image.width/2;
      float y1 = y - image.height/2;
      imageImpl(image,
                x1, y1, x1+image.width, y1+image.height,
                0, 0, image.width, image.height);
    }
  }


  /**
   * Displays images to the screen. The images must be in the sketch's "data"
   * directory to load correctly. Select "Add file..." from the "Sketch" menu
   * to add the image. Processing currently works with GIF, JPEG, and Targa
   * images. The color of an image may be modified with the <b>tint()</b>
   * function and if a GIF has transparency, it will maintain its transparency.
   * The <b>img</b> parameter specifies the image to display and the <b>x</b>
   * and <b>y</b> parameters define the location of the image from its
   * upper-left corner. The image is displayed at its original size unless
   * the <b>width</b> and <b>height</b> parameters specify a different size.
   * The <b>imageMode()</b> function changes the way the parameters work.
   * A call to <b>imageMode(CORNERS)</b> will change the width and height
   * parameters to define the x and y values of the opposite corner of the
   * image.
   *
   * =advanced
   * Starting with release 0124, when using the default (JAVA2D) renderer,
   * smooth() will also improve image quality of resized images.
   *
   * @webref image:loading_displaying
   * @param image the image to display
   * @param x x-coordinate of the image
   * @param y y-coordinate of the image
   * @param c width to display the image
   * @param d height to display the image
   *
   * @see processing.core.PApplet#loadImage(String, String)
   * @see processing.core.PImage
   * @see processing.core.PGraphics#imageMode(int)
   * @see processing.core.PGraphics#tint(float)
   * @see processing.core.PGraphics#background(float, float, float, float)
   * @see processing.core.PGraphics#alpha(int)
   */
  public void image(PImage image, float x, float y, float c, float d) {
    image(image, x, y, c, d, 0, 0, image.width, image.height);
  }


  /**
   * Draw an image(), also specifying u/v coordinates.
   * In this method, the  u, v coordinates are always based on image space
   * location, regardless of the current textureMode().
   */
  public void image(PImage image,
                    float a, float b, float c, float d,
                    int u1, int v1, int u2, int v2) {
    // Starting in release 0144, image errors are simply ignored.
    // loadImageAsync() sets width and height to -1 when loading fails.
    if (image.width == -1 || image.height == -1) return;

    if (imageMode == CORNER) {
      if (c < 0) {  // reset a negative width
        a += c; c = -c;
      }
      if (d < 0) {  // reset a negative height
        b += d; d = -d;
      }

      imageImpl(image,
                a, b, a + c, b + d,
                u1, v1, u2, v2);

    } else if (imageMode == CORNERS) {
      if (c < a) {  // reverse because x2 < x1
        float temp = a; a = c; c = temp;
      }
      if (d < b) {  // reverse because y2 < y1
        float temp = b; b = d; d = temp;
      }

      imageImpl(image,
                a, b, c, d,
                u1, v1, u2, v2);

    } else if (imageMode == CENTER) {
      // c and d are width/height
      if (c < 0) c = -c;
      if (d < 0) d = -d;
      float x1 = a - c/2;
      float y1 = b - d/2;

      imageImpl(image,
                x1, y1, x1 + c, y1 + d,
                u1, v1, u2, v2);
    }
  }


  /**
   * Expects x1, y1, x2, y2 coordinates where (x2 >= x1) and (y2 >= y1).
   * If tint() has been called, the image will be colored.
   * <p/>
   * The default implementation draws an image as a textured quad.
   * The (u, v) coordinates are in image space (they're ints, after all..)
   */
  protected void imageImpl(PImage image,
                           float x1, float y1, float x2, float y2,
                           int u1, int v1, int u2, int v2) {
    boolean savedStroke = stroke;
//    boolean savedFill = fill;
    int savedTextureMode = textureMode;

    stroke = false;
//    fill = true;
    textureMode = IMAGE;

//    float savedFillR = fillR;
//    float savedFillG = fillG;
//    float savedFillB = fillB;
//    float savedFillA = fillA;
//
//    if (tint) {
//      fillR = tintR;
//      fillG = tintG;
//      fillB = tintB;
//      fillA = tintA;
//
//    } else {
//      fillR = 1;
//      fillG = 1;
//      fillB = 1;
//      fillA = 1;
//    }

    beginShape(QUADS);
    texture(image);
    vertex(x1, y1, u1, v1);
    vertex(x1, y2, u1, v2);
    vertex(x2, y2, u2, v2);
    vertex(x2, y1, u2, v1);
    endShape();

    stroke = savedStroke;
//    fill = savedFill;
    textureMode = savedTextureMode;

//    fillR = savedFillR;
//    fillG = savedFillG;
//    fillB = savedFillB;
//    fillA = savedFillA;
  }



  //////////////////////////////////////////////////////////////

  // SHAPE


  /**
   * Modifies the location from which shapes draw.
   * The default mode is <b>shapeMode(CORNER)</b>, which specifies the
   * location to be the upper left corner of the shape and uses the third
   * and fourth parameters of <b>shape()</b> to specify the width and height.
   * The syntax <b>shapeMode(CORNERS)</b> uses the first and second parameters
   * of <b>shape()</b> to set the location of one corner and uses the third
   * and fourth parameters to set the opposite corner.
   * The syntax <b>shapeMode(CENTER)</b> draws the shape from its center point
   * and uses the third and forth parameters of <b>shape()</b> to specify the
   * width and height.
   * The parameter must be written in "ALL CAPS" because Processing syntax
   * is case sensitive.
   *
   * @param mode One of CORNER, CORNERS, CENTER
   *
   * @webref shape:loading_displaying
   * @see PGraphics#shape(PShape)
   * @see PGraphics#rectMode(int)
   */
  public void shapeMode(int mode) {
    this.shapeMode = mode;
  }


  public void shape(PShape shape) {
    if (shape.isVisible()) {  // don't do expensive matrix ops if invisible
      if (shapeMode == CENTER) {
        pushMatrix();
        translate(-shape.getWidth()/2, -shape.getHeight()/2);
      }

      shape.draw(this); // needs to handle recorder too

      if (shapeMode == CENTER) {
        popMatrix();
      }
    }
  }


  /**
   * Convenience method to draw at a particular location.
   */
  public void shape(PShape shape, float x, float y) {
    if (shape.isVisible()) {  // don't do expensive matrix ops if invisible
      pushMatrix();

      if (shapeMode == CENTER) {
        translate(x - shape.getWidth()/2, y - shape.getHeight()/2);

      } else if ((shapeMode == CORNER) || (shapeMode == CORNERS)) {
        translate(x, y);
      }
      shape.draw(this);

      popMatrix();
    }
  }


  /**
   * Displays shapes to the screen. The shapes must be in the sketch's "data"
   * directory to load correctly. Select "Add file..." from the "Sketch" menu
   * to add the shape.
   * Processing currently works with SVG shapes only.
   * The <b>sh</b> parameter specifies the shape to display and the <b>x</b>
   * and <b>y</b> parameters define the location of the shape from its
   * upper-left corner.
   * The shape is displayed at its original size unless the <b>width</b>
   * and <b>height</b> parameters specify a different size.
   * The <b>shapeMode()</b> function changes the way the parameters work.
   * A call to <b>shapeMode(CORNERS)</b>, for example, will change the width
   * and height parameters to define the x and y values of the opposite corner
   * of the shape.
   * <br><br>
   * Note complex shapes may draw awkwardly with P2D, P3D, and OPENGL. Those
   * renderers do not yet support shapes that have holes or complicated breaks.
   *
   * @param shape
   * @param x x-coordinate of the shape
   * @param y y-coordinate of the shape
   * @param c width to display the shape
   * @param d height to display the shape
   *
   * @webref shape:loading_displaying
   * @see PShape
   * @see PGraphics#loadShape(String)
   * @see PGraphics#shapeMode(int)
   */
  public void shape(PShape shape, float x, float y, float c, float d) {
    if (shape.isVisible()) {  // don't do expensive matrix ops if invisible
      pushMatrix();

      if (shapeMode == CENTER) {
        // x and y are center, c and d refer to a diameter
        translate(x - c/2f, y - d/2f);
        scale(c / shape.getWidth(), d / shape.getHeight());

      } else if (shapeMode == CORNER) {
        translate(x, y);
        scale(c / shape.getWidth(), d / shape.getHeight());

      } else if (shapeMode == CORNERS) {
        // c and d are x2/y2, make them into width/height
        c -= x;
        d -= y;
        // then same as above
        translate(x, y);
        scale(c / shape.getWidth(), d / shape.getHeight());
      }
      shape.draw(this);

      popMatrix();
    }
  }



  //////////////////////////////////////////////////////////////

  // TEXT/FONTS


  /**
   * Sets the alignment of the text to one of LEFT, CENTER, or RIGHT.
   * This will also reset the vertical text alignment to BASELINE.
   */
  public void textAlign(int align) {
    textAlign(align, BASELINE);
  }


  /**
   * Sets the horizontal and vertical alignment of the text. The horizontal
   * alignment can be one of LEFT, CENTER, or RIGHT. The vertical alignment
   * can be TOP, BOTTOM, CENTER, or the BASELINE (the default).
   */
  public void textAlign(int alignX, int alignY) {
    textAlign = alignX;
    textAlignY = alignY;
  }


  /**
   * Returns the ascent of the current font at the current size.
   * This is a method, rather than a variable inside the PGraphics object
   * because it requires calculation.
   */
  public float textAscent() {
    if (textFont == null) {
      defaultFontOrDeath("textAscent");
    }
    return textFont.ascent() * ((textMode == SCREEN) ? textFont.size : textSize);
  }


  /**
   * Returns the descent of the current font at the current size.
   * This is a method, rather than a variable inside the PGraphics object
   * because it requires calculation.
   */
  public float textDescent() {
    if (textFont == null) {
      defaultFontOrDeath("textDescent");
    }
    return textFont.descent() * ((textMode == SCREEN) ? textFont.size : textSize);
  }


  /**
   * Sets the current font. The font's size will be the "natural"
   * size of this font (the size that was set when using "Create Font").
   * The leading will also be reset.
   */
  public void textFont(PFont which) {
    if (which != null) {
      textFont = which;
      if (hints[ENABLE_NATIVE_FONTS]) {
        //if (which.font == null) {
        which.findFont();
        //}
      }
      /*
      textFontNative = which.font;

      //textFontNativeMetrics = null;
      // changed for rev 0104 for textMode(SHAPE) in opengl
      if (textFontNative != null) {
        // TODO need a better way to handle this. could use reflection to get
        // rid of the warning, but that'd be a little silly. supporting this is
        // an artifact of supporting java 1.1, otherwise we'd use getLineMetrics,
        // as recommended by the @deprecated flag.
        textFontNativeMetrics =
          Toolkit.getDefaultToolkit().getFontMetrics(textFontNative);
        // The following is what needs to be done, however we need to be able
        // to get the actual graphics context where the drawing is happening.
        // For instance, parent.getGraphics() doesn't work for OpenGL since
        // an OpenGL drawing surface is an embedded component.
//        if (parent != null) {
//          textFontNativeMetrics = parent.getGraphics().getFontMetrics(textFontNative);
//        }

        // float w = font.getStringBounds(text, g2.getFontRenderContext()).getWidth();
      }
      */
      textSize(which.size);

    } else {
      throw new RuntimeException(ERROR_TEXTFONT_NULL_PFONT);
    }
  }


  /**
   * Useful function to set the font and size at the same time.
   */
  public void textFont(PFont which, float size) {
    textFont(which);
    textSize(size);
  }


  /**
   * Set the text leading to a specific value. If using a custom
   * value for the text leading, you'll have to call textLeading()
   * again after any calls to textSize().
   */
  public void textLeading(float leading) {
    textLeading = leading;
  }


  /**
   * Sets the text rendering/placement to be either SCREEN (direct
   * to the screen, exact coordinates, only use the font's original size)
   * or MODEL (the default, where text is manipulated by translate() and
   * can have a textSize). The text size cannot be set when using
   * textMode(SCREEN), because it uses the pixels directly from the font.
   */
  public void textMode(int mode) {
    // CENTER and MODEL overlap (they're both 3)
    if ((mode == LEFT) || (mode == RIGHT)) {
      showWarning("Since Processing beta, textMode() is now textAlign().");
      return;
    }
//    if ((mode != SCREEN) && (mode != MODEL)) {
//      showError("Only textMode(SCREEN) and textMode(MODEL) " +
//      "are available with this renderer.");
//    }

    if (textModeCheck(mode)) {
      textMode = mode;
    } else {
      String modeStr = String.valueOf(mode);
      switch (mode) {
        case SCREEN: modeStr = "SCREEN"; break;
        case MODEL: modeStr = "MODEL"; break;
        case SHAPE: modeStr = "SHAPE"; break;
      }
      showWarning("textMode(" + modeStr + ") is not supported by this renderer.");
    }

    // reset the font to its natural size
    // (helps with width calculations and all that)
    //if (textMode == SCREEN) {
    //textSize(textFont.size);
    //}

    //} else {
    //throw new RuntimeException("use textFont() before textMode()");
    //}
  }


  protected boolean textModeCheck(int mode) {
    return true;
  }


  /**
   * Sets the text size, also resets the value for the leading.
   */
  public void textSize(float size) {
    if (textFont == null) {
      defaultFontOrDeath("textSize", size);
    }
    textSize = size;
    textLeading = (textAscent() + textDescent()) * 1.275f;
  }

  protected void beginTextScreenMode() {   
  }
  
  protected void endTextScreenMode() {    
  }  
  
  // ........................................................


  public float textWidth(char c) {
    textWidthBuffer[0] = c;
    return textWidthImpl(textWidthBuffer, 0, 1);
  }


  /**
   * Return the width of a line of text. If the text has multiple
   * lines, this returns the length of the longest line.
   */
  public float textWidth(String str) {
    if (textFont == null) {
      defaultFontOrDeath("textWidth");
    }

    int length = str.length();
    if (length > textWidthBuffer.length) {
      textWidthBuffer = new char[length + 10];
    }
    str.getChars(0, length, textWidthBuffer, 0);

    float wide = 0;
    int index = 0;
    int start = 0;

    while (index < length) {
      if (textWidthBuffer[index] == '\n') {
        wide = Math.max(wide, textWidthImpl(textWidthBuffer, start, index));
        start = index+1;
      }
      index++;
    }
    if (start < length) {
      wide = Math.max(wide, textWidthImpl(textWidthBuffer, start, index));
    }
    return wide;
  }


  /**
   * TODO not sure if this stays...
   */
  public float textWidth(char[] chars, int start, int length) {
    return textWidthImpl(chars, start, start + length);
  }


  /**
   * Implementation of returning the text width of
   * the chars [start, stop) in the buffer.
   * Unlike the previous version that was inside PFont, this will
   * return the size not of a 1 pixel font, but the actual current size.
   */
  protected float textWidthImpl(char buffer[], int start, int stop) {
    float wide = 0;
    for (int i = start; i < stop; i++) {
      // could add kerning here, but it just ain't implemented
      wide += textFont.width(buffer[i]) * textSize;
    }
    return wide;
  }


  // ........................................................


  /**
   * Write text where we just left off.
   */
  public void text(char c) {
    text(c, textX, textY, textZ);
  }


  /**
   * Draw a single character on screen.
   * Extremely slow when used with textMode(SCREEN) and Java 2D,
   * because loadPixels has to be called first and updatePixels last.
   */
  public void text(char c, float x, float y) {
    if (textFont == null) {
      defaultFontOrDeath("text");
    }

    if (textMode == SCREEN) beginTextScreenMode();

    if (textAlignY == CENTER) {
      y += textAscent() / 2;
    } else if (textAlignY == TOP) {
      y += textAscent();
    } else if (textAlignY == BOTTOM) {
      y -= textDescent();
    //} else if (textAlignY == BASELINE) {
      // do nothing
    }

    textBuffer[0] = c;
    textLineAlignImpl(textBuffer, 0, 1, x, y);

    if (textMode == SCREEN) endTextScreenMode();
  }


  /**
   * Draw a single character on screen (with a z coordinate)
   */
  public void text(char c, float x, float y, float z) {
//    if ((z != 0) && (textMode == SCREEN)) {
//      String msg = "textMode(SCREEN) cannot have a z coordinate";
//      throw new RuntimeException(msg);
//    }

    if (z != 0) translate(0, 0, z);  // slowness, badness

    text(c, x, y);
    textZ = z;

    if (z != 0) translate(0, 0, -z);
  }


  /**
   * Write text where we just left off.
   */
  public void text(String str) {
    text(str, textX, textY, textZ);
  }


  /**
   * Draw a chunk of text.
   * Newlines that are \n (Unix newline or linefeed char, ascii 10)
   * are honored, but \r (carriage return, Windows and Mac OS) are
   * ignored.
   */
  public void text(String str, float x, float y) {
    if (textFont == null) {
      defaultFontOrDeath("text");
    }

    int length = str.length();
    if (length > textBuffer.length) {
      textBuffer = new char[length + 10];
    }
    str.getChars(0, length, textBuffer, 0);
    text(textBuffer, 0, length, x, y);
  }


  /**
   * Method to draw text from an array of chars. This method will usually be
   * more efficient than drawing from a String object, because the String will
   * not be converted to a char array before drawing.
   */
  public void text(char[] chars, int start, int stop, float x, float y) {
    if (textMode == SCREEN) beginTextScreenMode();
    
    // If multiple lines, sum the height of the additional lines
    float high = 0; //-textAscent();
    for (int i = start; i < stop; i++) {
      if (chars[i] == '\n') {
        high += textLeading;
      }
    }
    if (textAlignY == CENTER) {
      // for a single line, this adds half the textAscent to y
      // for multiple lines, subtract half the additional height
      //y += (textAscent() - textDescent() - high)/2;
      y += (textAscent() - high)/2;
    } else if (textAlignY == TOP) {
      // for a single line, need to add textAscent to y
      // for multiple lines, no different
      y += textAscent();
    } else if (textAlignY == BOTTOM) {
      // for a single line, this is just offset by the descent
      // for multiple lines, subtract leading for each line
      y -= textDescent() + high;
    //} else if (textAlignY == BASELINE) {
      // do nothing
    }

//    int start = 0;
    int index = 0;
    while (index < stop) { //length) {
      if (chars[index] == '\n') {
        textLineAlignImpl(chars, start, index, x, y);
        start = index + 1;
        y += textLeading;
      }
      index++;
    }
    if (start < stop) {  //length) {
      textLineAlignImpl(chars, start, index, x, y);
    }
    
    if (textMode == SCREEN) endTextScreenMode();
  }


  /**
   * Same as above but with a z coordinate.
   */
  public void text(String str, float x, float y, float z) {
    if (z != 0) translate(0, 0, z);  // slow!

    text(str, x, y);
    textZ = z;

    if (z != 0) translate(0, 0, -z);  // inaccurate!
  }


  public void text(char[] chars, int start, int stop,
                   float x, float y, float z) {
    if (z != 0) translate(0, 0, z);  // slow!

    text(chars, start, stop, x, y);
    textZ = z;

    if (z != 0) translate(0, 0, -z);  // inaccurate!
  }


  /**
   * Draw text in a box that is constrained to a particular size.
   * The current rectMode() determines what the coordinates mean
   * (whether x1/y1/x2/y2 or x/y/w/h).
   * <P/>
   * Note that the x,y coords of the start of the box
   * will align with the *ascent* of the text, not the baseline,
   * as is the case for the other text() functions.
   * <P/>
   * Newlines that are \n (Unix newline or linefeed char, ascii 10)
   * are honored, and \r (carriage return, Windows and Mac OS) are
   * ignored.
   */
  public void text(String str, float x1, float y1, float x2, float y2) {
    if (textFont == null) {
      defaultFontOrDeath("text");
    }

    if (textMode == SCREEN) beginTextScreenMode();

    float hradius, vradius;
    switch (rectMode) {
    case CORNER:
      x2 += x1; y2 += y1;
      break;
    case RADIUS:
      hradius = x2;
      vradius = y2;
      x2 = x1 + hradius;
      y2 = y1 + vradius;
      x1 -= hradius;
      y1 -= vradius;
      break;
    case CENTER:
      hradius = x2 / 2.0f;
      vradius = y2 / 2.0f;
      x2 = x1 + hradius;
      y2 = y1 + vradius;
      x1 -= hradius;
      y1 -= vradius;
    }
    if (x2 < x1) {
      float temp = x1; x1 = x2; x2 = temp;
    }
    if (y2 < y1) {
      float temp = y1; y1 = y2; y2 = temp;
    }

//    float currentY = y1;
    float boxWidth = x2 - x1;

//    // ala illustrator, the text itself must fit inside the box
//    currentY += textAscent(); //ascent() * textSize;
//    // if the box is already too small, tell em to f off
//    if (currentY > y2) return;

    float spaceWidth = textWidth(' ');

    if (textBreakStart == null) {
      textBreakStart = new int[20];
      textBreakStop = new int[20];
    }
    textBreakCount = 0;

    int length = str.length();
    if (length + 1 > textBuffer.length) {
      textBuffer = new char[length + 1];
    }
    str.getChars(0, length, textBuffer, 0);
    // add a fake newline to simplify calculations
    textBuffer[length++] = '\n';

    int sentenceStart = 0;
    for (int i = 0; i < length; i++) {
      if (textBuffer[i] == '\n') {
//        currentY = textSentence(textBuffer, sentenceStart, i,
//                                lineX, boxWidth, currentY, y2, spaceWidth);
        boolean legit =
          textSentence(textBuffer, sentenceStart, i, boxWidth, spaceWidth);
        if (!legit) break;
//      if (Float.isNaN(currentY)) break;  // word too big (or error)
//      if (currentY > y2) break;  // past the box
        sentenceStart = i + 1;
      }
    }

    // lineX is the position where the text starts, which is adjusted
    // to left/center/right based on the current textAlign
    float lineX = x1; //boxX1;
    if (textAlign == CENTER) {
      lineX = lineX + boxWidth/2f;
    } else if (textAlign == RIGHT) {
      lineX = x2; //boxX2;
    }

    float boxHeight = y2 - y1;
    //int lineFitCount = 1 + PApplet.floor((boxHeight - textAscent()) / textLeading);
    // incorporate textAscent() for the top (baseline will be y1 + ascent)
    // and textDescent() for the bottom, so that lower parts of letters aren't
    // outside the box. [0151]
    float topAndBottom = textAscent() + textDescent();
    int lineFitCount = 1 + PApplet.floor((boxHeight - topAndBottom) / textLeading);
    int lineCount = Math.min(textBreakCount, lineFitCount);

    if (textAlignY == CENTER) {
      float lineHigh = textAscent() + textLeading * (lineCount - 1);
      float y = y1 + textAscent() + (boxHeight - lineHigh) / 2;
      for (int i = 0; i < lineCount; i++) {
        textLineAlignImpl(textBuffer, textBreakStart[i], textBreakStop[i], lineX, y);
        y += textLeading;
      }

    } else if (textAlignY == BOTTOM) {
      float y = y2 - textDescent() - textLeading * (lineCount - 1);
      for (int i = 0; i < lineCount; i++) {
        textLineAlignImpl(textBuffer, textBreakStart[i], textBreakStop[i], lineX, y);
        y += textLeading;
      }

    } else {  // TOP or BASELINE just go to the default
      float y = y1 + textAscent();
      for (int i = 0; i < lineCount; i++) {
        textLineAlignImpl(textBuffer, textBreakStart[i], textBreakStop[i], lineX, y);
        y += textLeading;
      }
    }

    if (textMode == SCREEN) endTextScreenMode();
  }


  /**
   * Emit a sentence of text, defined as a chunk of text without any newlines.
   * @param stop non-inclusive, the end of the text in question
   */
  protected boolean textSentence(char[] buffer, int start, int stop,
                                 float boxWidth, float spaceWidth) {
    float runningX = 0;

    // Keep track of this separately from index, since we'll need to back up
    // from index when breaking words that are too long to fit.
    int lineStart = start;
    int wordStart = start;
    int index = start;
    while (index <= stop) {
      // boundary of a word or end of this sentence
      if ((buffer[index] == ' ') || (index == stop)) {
        float wordWidth = textWidthImpl(buffer, wordStart, index);

        if (runningX + wordWidth > boxWidth) {
          if (runningX != 0) {
            // Next word is too big, output the current line and advance
            index = wordStart;
            textSentenceBreak(lineStart, index);
            // Eat whitespace because multiple spaces don't count for s*
            // when they're at the end of a line.
            while ((index < stop) && (buffer[index] == ' ')) {
              index++;
            }
          } else {  // (runningX == 0)
            // If this is the first word on the line, and its width is greater
            // than the width of the text box, then break the word where at the
            // max width, and send the rest of the word to the next line.
            do {
              index--;
              if (index == wordStart) {
                // Not a single char will fit on this line. screw 'em.
                //System.out.println("screw you");
                return false; //Float.NaN;
              }
              wordWidth = textWidthImpl(buffer, wordStart, index);
            } while (wordWidth > boxWidth);

            //textLineImpl(buffer, lineStart, index, x, y);
            textSentenceBreak(lineStart, index);
          }
          lineStart = index;
          wordStart = index;
          runningX = 0;

        } else if (index == stop) {
          // last line in the block, time to unload
          //textLineImpl(buffer, lineStart, index, x, y);
          textSentenceBreak(lineStart, index);
//          y += textLeading;
          index++;

        } else {  // this word will fit, just add it to the line
          runningX += wordWidth + spaceWidth;
          wordStart = index + 1;  // move on to the next word
          index++;
        }
      } else {  // not a space or the last character
        index++;  // this is just another letter
      }
    }
//    return y;
    return true;
  }


  protected void textSentenceBreak(int start, int stop) {
    if (textBreakCount == textBreakStart.length) {
      textBreakStart = PApplet.expand(textBreakStart);
      textBreakStop = PApplet.expand(textBreakStop);
    }
    textBreakStart[textBreakCount] = start;
    textBreakStop[textBreakCount] = stop;
    textBreakCount++;
  }


  public void text(String s, float x1, float y1, float x2, float y2, float z) {
    if (z != 0) translate(0, 0, z);  // slowness, badness

    text(s, x1, y1, x2, y2);
    textZ = z;

    if (z != 0) translate(0, 0, -z);  // TEMPORARY HACK! SLOW!
  }


  public void text(int num, float x, float y) {
    text(String.valueOf(num), x, y);
  }


  public void text(int num, float x, float y, float z) {
    text(String.valueOf(num), x, y, z);
  }


  /**
   * This does a basic number formatting, to avoid the
   * generally ugly appearance of printing floats.
   * Users who want more control should use their own nf() cmmand,
   * or if they want the long, ugly version of float,
   * use String.valueOf() to convert the float to a String first.
   */
  public void text(float num, float x, float y) {
    text(PApplet.nfs(num, 0, 3), x, y);
  }


  public void text(float num, float x, float y, float z) {
    text(PApplet.nfs(num, 0, 3), x, y, z);
  }

  
  //////////////////////////////////////////////////////////////

  // TEXT IMPL

  // These are most likely to be overridden by subclasses, since the other
  // (public) functions handle generic features like setting alignment.


  /**
   * Handles placement of a text line, then calls textLineImpl
   * to actually render at the specific point.
   */
  protected void textLineAlignImpl(char buffer[], int start, int stop,
                                   float x, float y) {
    if (textAlign == CENTER) {
      x -= textWidthImpl(buffer, start, stop) / 2f;

    } else if (textAlign == RIGHT) {
      x -= textWidthImpl(buffer, start, stop);
    }

    textLineImpl(buffer, start, stop, x, y);
  }


  /**
   * Implementation of actual drawing for a line of text.
   */
  protected void textLineImpl(char buffer[], int start, int stop,
                              float x, float y) {
    for (int index = start; index < stop; index++) {
      textCharImpl(buffer[index], x, y);

      // this doesn't account for kerning
      x += textWidth(buffer[index]);
    }
    textX = x;
    textY = y;
    textZ = 0;  // this will get set by the caller if non-zero
  }


  protected void textCharImpl(char ch, float x, float y) { //, float z) {
    PFont.Glyph glyph = textFont.getGlyph(ch);
    if (glyph != null) {
      if (textMode == MODEL) {
        float high    = glyph.height     / (float) textFont.size;
        float bwidth  = glyph.width      / (float) textFont.size;
        float lextent = glyph.leftExtent / (float) textFont.size;
        float textent = glyph.topExtent  / (float) textFont.size;

        float x1 = x + lextent * textSize;
        float y1 = y - textent * textSize;
        float x2 = x1 + bwidth * textSize;
        float y2 = y1 + high * textSize;

        textCharModelImpl(glyph.image,
                          x1, y1, x2, y2,
                          glyph.width, glyph.height);

      } else if (textMode == SCREEN) {
        int xx = (int) x + glyph.leftExtent;
        int yy = (int) y - glyph.topExtent;

        int w0 = glyph.width;
        int h0 = glyph.height;

        textCharScreenImpl(glyph.image, xx, yy, w0, h0);
      }
    }
  }


  protected void textCharModelImpl(PImage glyph,
                                   float x1, float y1, //float z1,
                                   float x2, float y2, //float z2,
                                   int u2, int v2) {
    boolean savedTint = tint;
    int savedTintColor = tintColor;
    float savedTintR = tintR;
    float savedTintG = tintG;
    float savedTintB = tintB;
    float savedTintA = tintA;
    boolean savedTintAlpha = tintAlpha;

    tint = true;
    tintColor = fillColor;
    tintR = fillR;
    tintG = fillG;
    tintB = fillB;
    tintA = fillA;
    tintAlpha = fillAlpha;

    imageImpl(glyph, x1, y1, x2, y2, 0, 0, u2, v2);

    tint = savedTint;
    tintColor = savedTintColor;
    tintR = savedTintR;
    tintG = savedTintG;
    tintB = savedTintB;
    tintA = savedTintA;
    tintAlpha = savedTintAlpha;
  }


  protected void textCharScreenImpl(PImage glyph,
                                    int xx, int yy,
                                    int w0, int h0) {
    int x0 = 0;
    int y0 = 0;

    if ((xx >= width) || (yy >= height) ||
        (xx + w0 < 0) || (yy + h0 < 0)) return;

    if (xx < 0) {
      x0 -= xx;
      w0 += xx;
      xx = 0;
    }
    if (yy < 0) {
      y0 -= yy;
      h0 += yy;
      yy = 0;
    }
    if (xx + w0 > width) {
      w0 -= ((xx + w0) - width);
    }
    if (yy + h0 > height) {
      h0 -= ((yy + h0) - height);
    }

    int fr = fillRi;
    int fg = fillGi;
    int fb = fillBi;
    int fa = fillAi;

    int pixels1[] = glyph.pixels; //images[glyph].pixels;

    // TODO this can be optimized a bit
    for (int row = y0; row < y0 + h0; row++) {
      for (int col = x0; col < x0 + w0; col++) {
        //int a1 = (fa * pixels1[row * textFont.twidth + col]) >> 8;
        int a1 = (fa * pixels1[row * glyph.width + col]) >> 8;
        int a2 = a1 ^ 0xff;
        //int p1 = pixels1[row * glyph.width + col];
        int p2 = pixels[(yy + row-y0)*width + (xx+col-x0)];

        pixels[(yy + row-y0)*width + xx+col-x0] =
          (0xff000000 |
           (((a1 * fr + a2 * ((p2 >> 16) & 0xff)) & 0xff00) << 8) |
           (( a1 * fg + a2 * ((p2 >>  8) & 0xff)) & 0xff00) |
           (( a1 * fb + a2 * ( p2        & 0xff)) >> 8));
      }
    }
  }



  //////////////////////////////////////////////////////////////

  // MATRIX STACK


  /**
   * Push a copy of the current transformation matrix onto the stack.
   */
  public void pushMatrix() {
    showMethodWarning("pushMatrix");
  }


  /**
   * Replace the current transformation matrix with the top of the stack.
   */
  public void popMatrix() {
    showMethodWarning("popMatrix");
  }



  //////////////////////////////////////////////////////////////

  // MATRIX TRANSFORMATIONS


  /**
   * Translate in X and Y.
   */
  public void translate(float tx, float ty) {
    showMissingWarning("translate");
  }


  /**
   * Translate in X, Y, and Z.
   */
  public void translate(float tx, float ty, float tz) {
    showMissingWarning("translate");
  }


  /**
   * Two dimensional rotation.
   *
   * Same as rotateZ (this is identical to a 3D rotation along the z-axis)
   * but included for clarity. It'd be weird for people drawing 2D graphics
   * to be using rotateZ. And they might kick our a-- for the confusion.
   *
   * <A HREF="http://www.xkcd.com/c184.html">Additional background</A>.
   */
  public void rotate(float angle) {
    showMissingWarning("rotate");
  }


  /**
   * Rotate around the X axis.
   */
  public void rotateX(float angle) {
    showMethodWarning("rotateX");
  }


  /**
   * Rotate around the Y axis.
   */
  public void rotateY(float angle) {
    showMethodWarning("rotateY");
  }


  /**
   * Rotate around the Z axis.
   *
   * The functions rotate() and rotateZ() are identical, it's just that it make
   * sense to have rotate() and then rotateX() and rotateY() when using 3D;
   * nor does it make sense to use a function called rotateZ() if you're only
   * doing things in 2D. so we just decided to have them both be the same.
   */
  public void rotateZ(float angle) {
    showMethodWarning("rotateZ");
  }


  /**
   * Rotate about a vector in space. Same as the glRotatef() function.
   */
  public void rotate(float angle, float vx, float vy, float vz) {
    showMissingWarning("rotate");
  }


  /**
   * Scale in all dimensions.
   */
  public void scale(float s) {
    showMissingWarning("scale");
  }


  /**
   * Scale in X and Y. Equivalent to scale(sx, sy, 1).
   *
   * Not recommended for use in 3D, because the z-dimension is just
   * scaled by 1, since there's no way to know what else to scale it by.
   */
  public void scale(float sx, float sy) {
    showMissingWarning("scale");
  }


  /**
   * Scale in X, Y, and Z.
   */
  public void scale(float x, float y, float z) {
    showMissingWarning("scale");
  }


  /**
   * Shear along X axis
   */
  public void shearX(float angle) {
    showMissingWarning("shearX");
  }


  /**
   * Shear along Y axis
   */
  public void shearY(float angle) {
    showMissingWarning("shearY");
  }


  //////////////////////////////////////////////////////////////

  // MATRIX FULL MONTY


  /**
   * Set the current transformation matrix to identity.
   */
  public void resetMatrix() {
    showMethodWarning("resetMatrix");
  }


  public void applyMatrix(PMatrix source) {
    if (source instanceof PMatrix2D) {
      applyMatrix((PMatrix2D) source);
    } else if (source instanceof PMatrix3D) {
      applyMatrix((PMatrix3D) source);
    }
  }


  public void applyMatrix(PMatrix2D source) {
    applyMatrix(source.m00, source.m01, source.m02,
                source.m10, source.m11, source.m12);
  }


  /**
   * Apply a 3x2 affine transformation matrix.
   */
  public void applyMatrix(float n00, float n01, float n02,
                          float n10, float n11, float n12) {
    showMissingWarning("applyMatrix");
  }


  public void applyMatrix(PMatrix3D source) {
    applyMatrix(source.m00, source.m01, source.m02, source.m03,
                source.m10, source.m11, source.m12, source.m13,
                source.m20, source.m21, source.m22, source.m23,
                source.m30, source.m31, source.m32, source.m33);
  }


  /**
   * Apply a 4x4 transformation matrix.
   */
  public void applyMatrix(float n00, float n01, float n02, float n03,
                          float n10, float n11, float n12, float n13,
                          float n20, float n21, float n22, float n23,
                          float n30, float n31, float n32, float n33) {
    showMissingWarning("applyMatrix");
  }



  //////////////////////////////////////////////////////////////

  // MATRIX GET/SET/PRINT


  public PMatrix getMatrix() {
    showMissingWarning("getMatrix");
    return null;
  }


  /**
   * Copy the current transformation matrix into the specified target.
   * Pass in null to create a new matrix.
   */
  public PMatrix2D getMatrix(PMatrix2D target) {
    showMissingWarning("getMatrix");
    return null;
  }


  /**
   * Copy the current transformation matrix into the specified target.
   * Pass in null to create a new matrix.
   */
  public PMatrix3D getMatrix(PMatrix3D target) {
    showMissingWarning("getMatrix");
    return null;
  }


  /**
   * Set the current transformation matrix to the contents of another.
   */
  public void setMatrix(PMatrix source) {
    if (source instanceof PMatrix2D) {
      setMatrix((PMatrix2D) source);
    } else if (source instanceof PMatrix3D) {
      setMatrix((PMatrix3D) source);
    }
  }


  /**
   * Set the current transformation to the contents of the specified source.
   */
  public void setMatrix(PMatrix2D source) {
    showMissingWarning("setMatrix");
  }


  /**
   * Set the current transformation to the contents of the specified source.
   */
  public void setMatrix(PMatrix3D source) {
    showMissingWarning("setMatrix");
  }


  /**
   * Print the current model (or "transformation") matrix.
   */
  public void printMatrix() {
    showMethodWarning("printMatrix");
  }


  //////////////////////////////////////////////////////////////

  // CAMERA


  public void beginCamera() {
    showMethodWarning("beginCamera");
  }


  public void endCamera() {
    showMethodWarning("endCamera");
  }


  public void camera() {
    showMissingWarning("camera");
  }


  public void camera(float eyeX, float eyeY, float eyeZ,
                     float centerX, float centerY, float centerZ,
                     float upX, float upY, float upZ) {
    showMissingWarning("camera");
  }


  public void printCamera() {
    showMethodWarning("printCamera");
  }



  //////////////////////////////////////////////////////////////

  // PROJECTION


  public void ortho() {
    showMissingWarning("ortho");
  }


  public void ortho(float left, float right,
                    float bottom, float top) {
    showMissingWarning("ortho");
  }


  public void ortho(float left, float right,
                    float bottom, float top,
                    float near, float far) {
    showMissingWarning("ortho");
  }


  public void perspective() {
    showMissingWarning("perspective");
  }


  public void perspective(float fovy, float aspect, float zNear, float zFar) {
    showMissingWarning("perspective");
  }


  public void frustum(float left, float right,
                      float bottom, float top,
                      float near, float far) {
    showMethodWarning("frustum");
  }


  public void printProjection() {
    showMethodWarning("printCamera");
  }



  //////////////////////////////////////////////////////////////

  // SCREEN TRANSFORMS


  /**
   * Given an x and y coordinate, returns the x position of where
   * that point would be placed on screen, once affected by translate(),
   * scale(), or any other transformations.
   */
  public float screenX(float x, float y) {
    showMissingWarning("screenX");
    return 0;
  }


  /**
   * Given an x and y coordinate, returns the y position of where
   * that point would be placed on screen, once affected by translate(),
   * scale(), or any other transformations.
   */
  public float screenY(float x, float y) {
    showMissingWarning("screenY");
    return 0;
  }


  /**
   * Maps a three dimensional point to its placement on-screen.
   * <P>
   * Given an (x, y, z) coordinate, returns the x position of where
   * that point would be placed on screen, once affected by translate(),
   * scale(), or any other transformations.
   */
  public float screenX(float x, float y, float z) {
    showMissingWarning("screenX");
    return 0;
  }


  /**
   * Maps a three dimensional point to its placement on-screen.
   * <P>
   * Given an (x, y, z) coordinate, returns the y position of where
   * that point would be placed on screen, once affected by translate(),
   * scale(), or any other transformations.
   */
  public float screenY(float x, float y, float z) {
    showMissingWarning("screenY");
    return 0;
  }


  /**
   * Maps a three dimensional point to its placement on-screen.
   * <P>
   * Given an (x, y, z) coordinate, returns its z value.
   * This value can be used to determine if an (x, y, z) coordinate
   * is in front or in back of another (x, y, z) coordinate.
   * The units are based on how the zbuffer is set up, and don't
   * relate to anything "real". They're only useful for in
   * comparison to another value obtained from screenZ(),
   * or directly out of the zbuffer[].
   */
  public float screenZ(float x, float y, float z) {
    showMissingWarning("screenZ");
    return 0;
  }


  /**
   * Returns the model space x value for an x, y, z coordinate.
   * <P>
   * This will give you a coordinate after it has been transformed
   * by translate(), rotate(), and camera(), but not yet transformed
   * by the projection matrix. For instance, his can be useful for
   * figuring out how points in 3D space relate to the edge
   * coordinates of a shape.
   */
  public float modelX(float x, float y, float z) {
    showMissingWarning("modelX");
    return 0;
  }


  /**
   * Returns the model space y value for an x, y, z coordinate.
   */
  public float modelY(float x, float y, float z) {
    showMissingWarning("modelY");
    return 0;
  }


  /**
   * Returns the model space z value for an x, y, z coordinate.
   */
  public float modelZ(float x, float y, float z) {
    showMissingWarning("modelZ");
    return 0;
  }



  //////////////////////////////////////////////////////////////

  // STYLE


  public void pushStyle() {
    if (styleStackDepth == styleStack.length) {
      styleStack = (PStyle[]) PApplet.expand(styleStack);
    }
    if (styleStack[styleStackDepth] == null) {
      styleStack[styleStackDepth] = new PStyle();
    }
    PStyle s = styleStack[styleStackDepth++];
    getStyle(s);
  }


  public void popStyle() {
    if (styleStackDepth == 0) {
      throw new RuntimeException("Too many popStyle() without enough pushStyle()");
    }
    styleStackDepth--;
    style(styleStack[styleStackDepth]);
  }


  public void style(PStyle s) {
    //  if (s.smooth) {
    //    smooth();
    //  } else {
    //    noSmooth();
    //  }

    imageMode(s.imageMode);
    rectMode(s.rectMode);
    ellipseMode(s.ellipseMode);
    shapeMode(s.shapeMode);

    if (s.tint) {
      tint(s.tintColor);
    } else {
      noTint();
    }
    if (s.fill) {
      fill(s.fillColor);
    } else {
      noFill();
    }
    if (s.stroke) {
      stroke(s.strokeColor);
    } else {
      noStroke();
    }
    strokeWeight(s.strokeWeight);
    strokeCap(s.strokeCap);
    strokeJoin(s.strokeJoin);

    // Set the colorMode() for the material properties.
    // TODO this is really inefficient, need to just have a material() method,
    // but this has the least impact to the API.
    colorMode(RGB, 1);
    ambient(s.ambientR, s.ambientG, s.ambientB);
    emissive(s.emissiveR, s.emissiveG, s.emissiveB);
    specular(s.specularR, s.specularG, s.specularB);
    shininess(s.shininess);

    /*
  s.ambientR = ambientR;
  s.ambientG = ambientG;
  s.ambientB = ambientB;
  s.specularR = specularR;
  s.specularG = specularG;
  s.specularB = specularB;
  s.emissiveR = emissiveR;
  s.emissiveG = emissiveG;
  s.emissiveB = emissiveB;
  s.shininess = shininess;
     */
    //  material(s.ambientR, s.ambientG, s.ambientB,
    //           s.emissiveR, s.emissiveG, s.emissiveB,
    //           s.specularR, s.specularG, s.specularB,
    //           s.shininess);

    // Set this after the material properties.
    colorMode(s.colorMode,
              s.colorModeX, s.colorModeY, s.colorModeZ, s.colorModeA);

    // This is a bit asymmetric, since there's no way to do "noFont()",
    // and a null textFont will produce an error (since usually that means that
    // the font couldn't load properly). So in some cases, the font won't be
    // 'cleared' to null, even though that's technically correct.
    if (s.textFont != null) {
      textFont(s.textFont, s.textSize);
      textLeading(s.textLeading);
    }
    // These don't require a font to be set.
    textAlign(s.textAlign, s.textAlignY);
    textMode(s.textMode);
  }


  public PStyle getStyle() {  // ignore
    return getStyle(null);
  }


  public PStyle getStyle(PStyle s) {  // ignore
    if (s == null) {
      s = new PStyle();
    }

    s.imageMode = imageMode;
    s.rectMode = rectMode;
    s.ellipseMode = ellipseMode;
    s.shapeMode = shapeMode;

    s.colorMode = colorMode;
    s.colorModeX = colorModeX;
    s.colorModeY = colorModeY;
    s.colorModeZ = colorModeZ;
    s.colorModeA = colorModeA;

    s.tint = tint;
    s.tintColor = tintColor;
    s.fill = fill;
    s.fillColor = fillColor;
    s.stroke = stroke;
    s.strokeColor = strokeColor;
    s.strokeWeight = strokeWeight;
    s.strokeCap = strokeCap;
    s.strokeJoin = strokeJoin;

    s.ambientR = ambientR;
    s.ambientG = ambientG;
    s.ambientB = ambientB;
    s.specularR = specularR;
    s.specularG = specularG;
    s.specularB = specularB;
    s.emissiveR = emissiveR;
    s.emissiveG = emissiveG;
    s.emissiveB = emissiveB;
    s.shininess = shininess;

    s.textFont = textFont;
    s.textAlign = textAlign;
    s.textAlignY = textAlignY;
    s.textMode = textMode;
    s.textSize = textSize;
    s.textLeading = textLeading;

    return s;
  }



  //////////////////////////////////////////////////////////////

  // STROKE CAP/JOIN/WEIGHT


  public void strokeWeight(float weight) {
    strokeWeight = weight;
  }


  public void strokeJoin(int join) {
    strokeJoin = join;
  }


  public void strokeCap(int cap) {
    strokeCap = cap;
  }



  //////////////////////////////////////////////////////////////

  // STROKE COLOR


  /**
   * Disables drawing the stroke (outline). If both <b>noStroke()</b> and
   * <b>noFill()</b> are called, no shapes will be drawn to the screen.
   *
   * @webref color:setting
   *
   * @see PGraphics#stroke(float, float, float, float)
   */
  public void noStroke() {
    stroke = false;
  }


  /**
   * Set the tint to either a grayscale or ARGB value.
   * See notes attached to the fill() function.
   * @param rgb color value in hexadecimal notation
   * (i.e. #FFCC00 or 0xFFFFCC00) or any value of the color datatype
   */
  public void stroke(int rgb) {
    colorCalc(rgb);
    strokeFromCalc();
  }


  public void stroke(int rgb, float alpha) {
    colorCalc(rgb, alpha);
    strokeFromCalc();
  }


  /**
   *
   * @param gray specifies a value between white and black
   */
  public void stroke(float gray) {
    colorCalc(gray);
    strokeFromCalc();
  }


  public void stroke(float gray, float alpha) {
    colorCalc(gray, alpha);
    strokeFromCalc();
  }


  public void stroke(float x, float y, float z) {
    colorCalc(x, y, z);
    strokeFromCalc();
  }


  /**
   * Sets the color used to draw lines and borders around shapes. This color
   * is either specified in terms of the RGB or HSB color depending on the
   * current <b>colorMode()</b> (the default color space is RGB, with each
   * value in the range from 0 to 255).
   * <br><br>When using hexadecimal notation to specify a color, use "#" or
   * "0x" before the values (e.g. #CCFFAA, 0xFFCCFFAA). The # syntax uses six
   * digits to specify a color (the way colors are specified in HTML and CSS).
   * When using the hexadecimal notation starting with "0x", the hexadecimal
   * value must be specified with eight characters; the first two characters
   * define the alpha component and the remainder the red, green, and blue
   * components.
   * <br><br>The value for the parameter "gray" must be less than or equal
   * to the current maximum value as specified by <b>colorMode()</b>.
   * The default maximum value is 255.
   *
   * @webref color:setting
   * @param alpha opacity of the stroke
   * @param x red or hue value (depending on the current color mode)
   * @param y green or saturation value (depending on the current color mode)
   * @param z blue or brightness value (depending on the current color mode)
   */
  public void stroke(float x, float y, float z, float a) {
    colorCalc(x, y, z, a);
    strokeFromCalc();
  }


  protected void strokeFromCalc() {
    stroke = true;
    strokeR = calcR;
    strokeG = calcG;
    strokeB = calcB;
    strokeA = calcA;
    strokeRi = calcRi;
    strokeGi = calcGi;
    strokeBi = calcBi;
    strokeAi = calcAi;
    strokeColor = calcColor;
    strokeAlpha = calcAlpha;
  }



  //////////////////////////////////////////////////////////////

  // TINT COLOR


  /**
   * Removes the current fill value for displaying images and reverts to displaying images with their original hues.
   *
   * @webref image:loading_displaying
   * @see processing.core.PGraphics#tint(float, float, float, float)
   * @see processing.core.PGraphics#image(PImage, float, float, float, float)
   */
  public void noTint() {
    tint = false;
  }


  /**
   * Set the tint to either a grayscale or ARGB value.
   */
  public void tint(int rgb) {
    colorCalc(rgb);
    tintFromCalc();
  }


  /**
   * @param rgb color value in hexadecimal notation
   * (i.e. #FFCC00 or 0xFFFFCC00) or any value of the color datatype
   * @param alpha opacity of the image
   */
  public void tint(int rgb, float alpha) {
    colorCalc(rgb, alpha);
    tintFromCalc();
  }


  /**
   * @param gray any valid number
   */
  public void tint(float gray) {
    colorCalc(gray);
    tintFromCalc();
  }


  public void tint(float gray, float alpha) {
    colorCalc(gray, alpha);
    tintFromCalc();
  }


  public void tint(float x, float y, float z) {
    colorCalc(x, y, z);
    tintFromCalc();
  }


  /**
   * Sets the fill value for displaying images. Images can be tinted to
   * specified colors or made transparent by setting the alpha.
   * <br><br>To make an image transparent, but not change it's color,
   * use white as the tint color and specify an alpha value. For instance,
   * tint(255, 128) will make an image 50% transparent (unless
   * <b>colorMode()</b> has been used).
   *
   * <br><br>When using hexadecimal notation to specify a color, use "#" or
   * "0x" before the values (e.g. #CCFFAA, 0xFFCCFFAA). The # syntax uses six
   * digits to specify a color (the way colors are specified in HTML and CSS).
   * When using the hexadecimal notation starting with "0x", the hexadecimal
   * value must be specified with eight characters; the first two characters
   * define the alpha component and the remainder the red, green, and blue
   * components.
   * <br><br>The value for the parameter "gray" must be less than or equal
   * to the current maximum value as specified by <b>colorMode()</b>.
   * The default maximum value is 255.
   * <br><br>The tint() method is also used to control the coloring of
   * textures in 3D.
   *
   * @webref image:loading_displaying
   * @param x red or hue value
   * @param y green or saturation value
   * @param z blue or brightness value
   *
   * @see processing.core.PGraphics#noTint()
   * @see processing.core.PGraphics#image(PImage, float, float, float, float)
   */
  public void tint(float x, float y, float z, float a) {
    colorCalc(x, y, z, a);
    tintFromCalc();
  }


  protected void tintFromCalc() {
    tint = true;
    tintR = calcR;
    tintG = calcG;
    tintB = calcB;
    tintA = calcA;
    tintRi = calcRi;
    tintGi = calcGi;
    tintBi = calcBi;
    tintAi = calcAi;
    tintColor = calcColor;
    tintAlpha = calcAlpha;
  }



  //////////////////////////////////////////////////////////////

  // FILL COLOR


  /**
   * Disables filling geometry. If both <b>noStroke()</b> and <b>noFill()</b>
   * are called, no shapes will be drawn to the screen.
   *
   * @webref color:setting
   *
   * @see PGraphics#fill(float, float, float, float)
   *
   */
  public void noFill() {
    fill = false;
  }


  /**
   * Set the fill to either a grayscale value or an ARGB int.
   * @param rgb color value in hexadecimal notation (i.e. #FFCC00 or 0xFFFFCC00) or any value of the color datatype
   */
  public void fill(int rgb) {
    colorCalc(rgb);
    fillFromCalc();
  }


  public void fill(int rgb, float alpha) {
    colorCalc(rgb, alpha);
    fillFromCalc();
  }


  /**
   * @param gray number specifying value between white and black
   */
  public void fill(float gray) {
    colorCalc(gray);
    fillFromCalc();
  }


  public void fill(float gray, float alpha) {
    colorCalc(gray, alpha);
    fillFromCalc();
  }


  public void fill(float x, float y, float z) {
    colorCalc(x, y, z);
    fillFromCalc();
  }


  /**
   * Sets the color used to fill shapes. For example, if you run <b>fill(204, 102, 0)</b>, all subsequent shapes will be filled with orange. This color is either specified in terms of the RGB or HSB color depending on the current <b>colorMode()</b> (the default color space is RGB, with each value in the range from 0 to 255).
   * <br><br>When using hexadecimal notation to specify a color, use "#" or "0x" before the values (e.g. #CCFFAA, 0xFFCCFFAA). The # syntax uses six digits to specify a color (the way colors are specified in HTML and CSS). When using the hexadecimal notation starting with "0x", the hexadecimal value must be specified with eight characters; the first two characters define the alpha component and the remainder the red, green, and blue components.
   * <br><br>The value for the parameter "gray" must be less than or equal to the current maximum value as specified by <b>colorMode()</b>. The default maximum value is 255.
   * <br><br>To change the color of an image (or a texture), use tint().
   *
   * @webref color:setting
   * @param x red or hue value
   * @param y green or saturation value
   * @param z blue or brightness value
   * @param alpha opacity of the fill
   *
   * @see PGraphics#noFill()
   * @see PGraphics#stroke(float)
   * @see PGraphics#tint(float)
   * @see PGraphics#background(float, float, float, float)
   * @see PGraphics#colorMode(int, float, float, float, float)
   */
  public void fill(float x, float y, float z, float a) {
    colorCalc(x, y, z, a);
    fillFromCalc();
  }


  protected void fillFromCalc() {
    fill = true;
    fillR = calcR;
    fillG = calcG;
    fillB = calcB;
    fillA = calcA;
    fillRi = calcRi;
    fillGi = calcGi;
    fillBi = calcBi;
    fillAi = calcAi;
    fillColor = calcColor;
    fillAlpha = calcAlpha;
  }



  //////////////////////////////////////////////////////////////

  // MATERIAL PROPERTIES


  public void ambient(int rgb) {
//    if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
//      ambient((float) rgb);
//
//    } else {
//      colorCalcARGB(rgb, colorModeA);
//      ambientFromCalc();
//    }
    colorCalc(rgb);
    ambientFromCalc();
  }


  public void ambient(float gray) {
    colorCalc(gray);
    ambientFromCalc();
  }


  public void ambient(float x, float y, float z) {
    colorCalc(x, y, z);
    ambientFromCalc();
  }


  protected void ambientFromCalc() {
    ambientR = calcR;
    ambientG = calcG;
    ambientB = calcB;
  }


  public void specular(int rgb) {
//    if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
//      specular((float) rgb);
//
//    } else {
//      colorCalcARGB(rgb, colorModeA);
//      specularFromCalc();
//    }
    colorCalc(rgb);
    specularFromCalc();
  }


  public void specular(float gray) {
    colorCalc(gray);
    specularFromCalc();
  }


  public void specular(float x, float y, float z) {
    colorCalc(x, y, z);
    specularFromCalc();
  }


  protected void specularFromCalc() {
    specularR = calcR;
    specularG = calcG;
    specularB = calcB;
  }


  public void shininess(float shine) {
    shininess = shine;
  }


  public void emissive(int rgb) {
//    if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
//      emissive((float) rgb);
//
//    } else {
//      colorCalcARGB(rgb, colorModeA);
//      emissiveFromCalc();
//    }
    colorCalc(rgb);
    emissiveFromCalc();
  }


  public void emissive(float gray) {
    colorCalc(gray);
    emissiveFromCalc();
  }


  public void emissive(float x, float y, float z) {
    colorCalc(x, y, z);
    emissiveFromCalc();
  }


  protected void emissiveFromCalc() {
    emissiveR = calcR;
    emissiveG = calcG;
    emissiveB = calcB;
  }



  //////////////////////////////////////////////////////////////

  // LIGHTS

  // The details of lighting are very implementation-specific, so this base
  // class does not handle any details of settings lights. It does however
  // display warning messages that the functions are not available.


  public void lights() {
    showMethodWarning("lights");
  }

  public void noLights() {
    showMethodWarning("noLights");
  }

  public void ambientLight(float red, float green, float blue) {
    showMethodWarning("ambientLight");
  }

  public void ambientLight(float red, float green, float blue,
                           float x, float y, float z) {
    showMethodWarning("ambientLight");
  }

  public void directionalLight(float red, float green, float blue,
                               float nx, float ny, float nz) {
    showMethodWarning("directionalLight");
  }

  public void pointLight(float red, float green, float blue,
                         float x, float y, float z) {
    showMethodWarning("pointLight");
  }

  public void spotLight(float red, float green, float blue,
                        float x, float y, float z,
                        float nx, float ny, float nz,
                        float angle, float concentration) {
    showMethodWarning("spotLight");
  }

  public void lightFalloff(float constant, float linear, float quadratic) {
    showMethodWarning("lightFalloff");
  }

  public void lightSpecular(float x, float y, float z) {
    showMethodWarning("lightSpecular");
  }



  //////////////////////////////////////////////////////////////

  // BACKGROUND


  /**
   * Set the background to a gray or ARGB color.
   * <p>
   * For the main drawing surface, the alpha value will be ignored. However,
   * alpha can be used on PGraphics objects from createGraphics(). This is
   * the only way to set all the pixels partially transparent, for instance.
   * <p>
   * Note that background() should be called before any transformations occur,
   * because some implementations may require the current transformation matrix
   * to be identity before drawing.
   *
   * @param rgb color value in hexadecimal notation (i.e. #FFCC00 or 0xFFFFCC00)<br/>or any value of the color datatype
   */
  public void background(int rgb) {
//    if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
//      background((float) rgb);
//
//    } else {
//      if (format == RGB) {
//        rgb |= 0xff000000;  // ignore alpha for main drawing surface
//      }
//      colorCalcARGB(rgb, colorModeA);
//      backgroundFromCalc();
//      backgroundImpl();
//    }
    colorCalc(rgb);
    backgroundFromCalc();
  }


  /**
   * See notes about alpha in background(x, y, z, a).
   */
  public void background(int rgb, float alpha) {
//    if (format == RGB) {
//      background(rgb);  // ignore alpha for main drawing surface
//
//    } else {
//      if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
//        background((float) rgb, alpha);
//
//      } else {
//        colorCalcARGB(rgb, alpha);
//        backgroundFromCalc();
//        backgroundImpl();
//      }
//    }
    colorCalc(rgb, alpha);
    backgroundFromCalc();
  }


  /**
   * Set the background to a grayscale value, based on the
   * current colorMode.
   */
  public void background(float gray) {
    colorCalc(gray);
    backgroundFromCalc();
//    backgroundImpl();
  }


  /**
   * See notes about alpha in background(x, y, z, a).
   * @param gray specifies a value between white and black
   * @param alpha opacity of the background
   */
  public void background(float gray, float alpha) {
    if (format == RGB) {
      background(gray);  // ignore alpha for main drawing surface

    } else {
      colorCalc(gray, alpha);
      backgroundFromCalc();
//      backgroundImpl();
    }
  }


  /**
   * Set the background to an r, g, b or h, s, b value,
   * based on the current colorMode.
   */
  public void background(float x, float y, float z) {
    colorCalc(x, y, z);
    backgroundFromCalc();
//    backgroundImpl();
  }


  /**
   * The <b>background()</b> function sets the color used for the background of the Processing window. The default background is light gray. In the <b>draw()</b> function, the background color is used to clear the display window at the beginning of each frame.
   * <br><br>An image can also be used as the background for a sketch, however its width and height must be the same size as the sketch window. To resize an image 'b' to the size of the sketch window, use b.resize(width, height).
   * <br><br>Images used as background will ignore the current tint() setting.
   * <br><br>It is not possible to use transparency (alpha) in background colors with the main drawing surface, however they will work properly with <b>createGraphics</b>.
   *
   * =advanced
   * <p>Clear the background with a color that includes an alpha value. This can
   * only be used with objects created by createGraphics(), because the main
   * drawing surface cannot be set transparent.</p>
   * <p>It might be tempting to use this function to partially clear the screen
   * on each frame, however that's not how this function works. When calling
   * background(), the pixels will be replaced with pixels that have that level
   * of transparency. To do a semi-transparent overlay, use fill() with alpha
   * and draw a rectangle.</p>
   *
   * @webref color:setting
   * @param x red or hue value (depending on the current color mode)
   * @param y green or saturation value (depending on the current color mode)
   * @param z blue or brightness value (depending on the current color mode)
   *
   * @see PGraphics#stroke(float)
   * @see PGraphics#fill(float)
   * @see PGraphics#tint(float)
   * @see PGraphics#colorMode(int)
   */
  public void background(float x, float y, float z, float a) {
//    if (format == RGB) {
//      background(x, y, z);  // don't allow people to set alpha
//
//    } else {
//      colorCalc(x, y, z, a);
//      backgroundFromCalc();
//      backgroundImpl();
//    }
    colorCalc(x, y, z, a);
    backgroundFromCalc();
  }


  protected void backgroundFromCalc() {
    backgroundR = calcR;
    backgroundG = calcG;
    backgroundB = calcB;
    backgroundA = (format == RGB) ? colorModeA : calcA;
    backgroundRi = calcRi;
    backgroundGi = calcGi;
    backgroundBi = calcBi;
    backgroundAi = (format == RGB) ? 255 : calcAi;
    backgroundAlpha = (format == RGB) ? false : calcAlpha;
    backgroundColor = calcColor;

    backgroundImpl();
  }


  /**
   * Takes an RGB or ARGB image and sets it as the background.
   * The width and height of the image must be the same size as the sketch.
   * Use image.resize(width, height) to make short work of such a task.
   * <P>
   * Note that even if the image is set as RGB, the high 8 bits of each pixel
   * should be set opaque (0xFF000000), because the image data will be copied
   * directly to the screen, and non-opaque background images may have strange
   * behavior. Using image.filter(OPAQUE) will handle this easily.
   * <P>
   * When using 3D, this will also clear the zbuffer (if it exists).
   */
  public void background(PImage image) {
    if ((image.width != width) || (image.height != height)) {
      throw new RuntimeException(ERROR_BACKGROUND_IMAGE_SIZE);
    }
    if ((image.format != RGB) && (image.format != ARGB)) {
      throw new RuntimeException(ERROR_BACKGROUND_IMAGE_FORMAT);
    }
    backgroundColor = 0;  // just zero it out for images
    backgroundImpl(image);
  }


  /**
   * Actually set the background image. This is separated from the error
   * handling and other semantic goofiness that is shared across renderers.
   */
  protected void backgroundImpl(PImage image) {
    // blit image to the screen
    set(0, 0, image);
  }


  /**
   * Actual implementation of clearing the background, now that the
   * internal variables for background color have been set. Called by the
   * backgroundFromCalc() method, which is what all the other background()
   * methods call once the work is done.
   */
  protected void backgroundImpl() {
    pushStyle();
    pushMatrix();
    resetMatrix();
    fill(backgroundColor);
    rect(0, 0, width, height);
    popMatrix();
    popStyle();
  }


  /**
   * Callback to handle clearing the background when begin/endRaw is in use.
   * Handled as separate function for OpenGL (or other) subclasses that
   * override backgroundImpl() but still needs this to work properly.
   */
//  protected void backgroundRawImpl() {
//    if (raw != null) {
//      raw.colorMode(RGB, 1);
//      raw.noStroke();
//      raw.fill(backgroundR, backgroundG, backgroundB);
//      raw.beginShape(TRIANGLES);
//
//      raw.vertex(0, 0);
//      raw.vertex(width, 0);
//      raw.vertex(0, height);
//
//      raw.vertex(width, 0);
//      raw.vertex(width, height);
//      raw.vertex(0, height);
//
//      raw.endShape();
//    }
//  }



  //////////////////////////////////////////////////////////////

  // COLOR MODE


  /**
   * @param mode Either RGB or HSB, corresponding to Red/Green/Blue and Hue/Saturation/Brightness
   * @param max range for all color elements
   */
  public void colorMode(int mode) {
    colorMode(mode, colorModeX, colorModeY, colorModeZ, colorModeA);
  }


  public void colorMode(int mode, float max) {
    colorMode(mode, max, max, max, max);
  }


  /**
   * Set the colorMode and the maximum values for (r, g, b)
   * or (h, s, b).
   * <P>
   * Note that this doesn't set the maximum for the alpha value,
   * which might be confusing if for instance you switched to
   * <PRE>colorMode(HSB, 360, 100, 100);</PRE>
   * because the alpha values were still between 0 and 255.
   */
  public void colorMode(int mode, float maxX, float maxY, float maxZ) {
    colorMode(mode, maxX, maxY, maxZ, colorModeA);
  }


  /**
   * Changes the way Processing interprets color data. By default, the parameters for <b>fill()</b>, <b>stroke()</b>, <b>background()</b>, and <b>color()</b> are defined by values between 0 and 255 using the RGB color model. The <b>colorMode()</b> function is used to change the numerical range used for specifying colors and to switch color systems. For example, calling <b>colorMode(RGB, 1.0)</b> will specify that values are specified between 0 and 1. The limits for defining colors are altered by setting the parameters range1, range2, range3, and range 4.
   *
   * @webref color:setting
   * @param maxX range for the red or hue depending on the current color mode
   * @param maxY range for the green or saturation depending on the current color mode
   * @param maxZ range for the blue or brightness depending on the current color mode
   * @param maxA range for the alpha
   *
   * @see PGraphics#background(float)
   * @see PGraphics#fill(float)
   * @see PGraphics#stroke(float)
   */
  public void colorMode(int mode,
                        float maxX, float maxY, float maxZ, float maxA) {
    colorMode = mode;

    colorModeX = maxX;  // still needs to be set for hsb
    colorModeY = maxY;
    colorModeZ = maxZ;
    colorModeA = maxA;

    // if color max values are all 1, then no need to scale
    colorModeScale =
      ((maxA != 1) || (maxX != maxY) || (maxY != maxZ) || (maxZ != maxA));

    // if color is rgb/0..255 this will make it easier for the
    // red() green() etc functions
    colorModeDefault = (colorMode == RGB) &&
      (colorModeA == 255) && (colorModeX == 255) &&
      (colorModeY == 255) && (colorModeZ == 255);
  }



  //////////////////////////////////////////////////////////////

  // COLOR CALCULATIONS

  // Given input values for coloring, these functions will fill the calcXxxx
  // variables with values that have been properly filtered through the
  // current colorMode settings.

  // Renderers that need to subclass any drawing properties such as fill or
  // stroke will usally want to override methods like fillFromCalc (or the
  // same for stroke, ambient, etc.) That way the color calcuations are
  // covered by this based PGraphics class, leaving only a single function
  // to override/implement in the subclass.


  /**
   * Set the fill to either a grayscale value or an ARGB int.
   * <P>
   * The problem with this code is that it has to detect between these two
   * situations automatically. This is done by checking to see if the high bits
   * (the alpha for 0xAA000000) is set, and if not, whether the color value
   * that follows is less than colorModeX (first param passed to colorMode).
   * <P>
   * This auto-detect would break in the following situation:
   * <PRE>size(256, 256);
   * for (int i = 0; i < 256; i++) {
   *   color c = color(0, 0, 0, i);
   *   stroke(c);
   *   line(i, 0, i, 256);
   * }</PRE>
   * ...on the first time through the loop, where (i == 0), since the color
   * itself is zero (black) then it would appear indistinguishable from code
   * that reads "fill(0)". The solution is to use the four parameter versions
   * of stroke or fill to more directly specify the desired result.
   */
  protected void colorCalc(int rgb) {
    if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
      colorCalc((float) rgb);

    } else {
      colorCalcARGB(rgb, colorModeA);
    }
  }


  protected void colorCalc(int rgb, float alpha) {
    if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {  // see above
      colorCalc((float) rgb, alpha);

    } else {
      colorCalcARGB(rgb, alpha);
    }
  }


  protected void colorCalc(float gray) {
    colorCalc(gray, colorModeA);
  }


  protected void colorCalc(float gray, float alpha) {
    if (gray > colorModeX) gray = colorModeX;
    if (alpha > colorModeA) alpha = colorModeA;

    if (gray < 0) gray = 0;
    if (alpha < 0) alpha = 0;

    calcR = colorModeScale ? (gray / colorModeX) : gray;
    calcG = calcR;
    calcB = calcR;
    calcA = colorModeScale ? (alpha / colorModeA) : alpha;

    calcRi = (int)(calcR*255); calcGi = (int)(calcG*255);
    calcBi = (int)(calcB*255); calcAi = (int)(calcA*255);
    calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
    calcAlpha = (calcAi != 255);
  }


  protected void colorCalc(float x, float y, float z) {
    colorCalc(x, y, z, colorModeA);
  }


  protected void colorCalc(float x, float y, float z, float a) {
    if (x > colorModeX) x = colorModeX;
    if (y > colorModeY) y = colorModeY;
    if (z > colorModeZ) z = colorModeZ;
    if (a > colorModeA) a = colorModeA;

    if (x < 0) x = 0;
    if (y < 0) y = 0;
    if (z < 0) z = 0;
    if (a < 0) a = 0;

    switch (colorMode) {
    case RGB:
      if (colorModeScale) {
        calcR = x / colorModeX;
        calcG = y / colorModeY;
        calcB = z / colorModeZ;
        calcA = a / colorModeA;
      } else {
        calcR = x; calcG = y; calcB = z; calcA = a;
      }
      break;

    case HSB:
      x /= colorModeX; // h
      y /= colorModeY; // s
      z /= colorModeZ; // b

      calcA = colorModeScale ? (a/colorModeA) : a;

      if (y == 0) {  // saturation == 0
        calcR = calcG = calcB = z;

      } else {
        float which = (x - (int)x) * 6.0f;
        float f = which - (int)which;
        float p = z * (1.0f - y);
        float q = z * (1.0f - y * f);
        float t = z * (1.0f - (y * (1.0f - f)));

        switch ((int)which) {
        case 0: calcR = z; calcG = t; calcB = p; break;
        case 1: calcR = q; calcG = z; calcB = p; break;
        case 2: calcR = p; calcG = z; calcB = t; break;
        case 3: calcR = p; calcG = q; calcB = z; break;
        case 4: calcR = t; calcG = p; calcB = z; break;
        case 5: calcR = z; calcG = p; calcB = q; break;
        }
      }
      break;
    }
    calcRi = (int)(255*calcR); calcGi = (int)(255*calcG);
    calcBi = (int)(255*calcB); calcAi = (int)(255*calcA);
    calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
    calcAlpha = (calcAi != 255);
  }


  /**
   * Unpacks AARRGGBB color for direct use with colorCalc.
   * <P>
   * Handled here with its own function since this is indepenent
   * of the color mode.
   * <P>
   * Strangely the old version of this code ignored the alpha
   * value. not sure if that was a bug or what.
   * <P>
   * Note, no need for a bounds check since it's a 32 bit number.
   */
  protected void colorCalcARGB(int argb, float alpha) {
    if (alpha == colorModeA) {
      calcAi = (argb >> 24) & 0xff;
      calcColor = argb;
    } else {
      calcAi = (int) (((argb >> 24) & 0xff) * (alpha / colorModeA));
      calcColor = (calcAi << 24) | (argb & 0xFFFFFF);
    }
    calcRi = (argb >> 16) & 0xff;
    calcGi = (argb >> 8) & 0xff;
    calcBi = argb & 0xff;
    calcA = (float)calcAi / 255.0f;
    calcR = (float)calcRi / 255.0f;
    calcG = (float)calcGi / 255.0f;
    calcB = (float)calcBi / 255.0f;
    calcAlpha = (calcAi != 255);
  }



  //////////////////////////////////////////////////////////////

  // COLOR DATATYPE STUFFING

  // The 'color' primitive type in Processing syntax is in fact a 32-bit int.
  // These functions handle stuffing color values into a 32-bit cage based
  // on the current colorMode settings.

  // These functions are really slow (because they take the current colorMode
  // into account), but they're easy to use. Advanced users can write their
  // own bit shifting operations to setup 'color' data types.


  public final int color(int c) {  // ignore
//    if (((c & 0xff000000) == 0) && (c <= colorModeX)) {
//      if (colorModeDefault) {
//        // bounds checking to make sure the numbers aren't to high or low
//        if (c > 255) c = 255; else if (c < 0) c = 0;
//        return 0xff000000 | (c << 16) | (c << 8) | c;
//      } else {
//        colorCalc(c);
//      }
//    } else {
//      colorCalcARGB(c, colorModeA);
//    }
    colorCalc(c);
    return calcColor;
  }


  public final int color(float gray) {  // ignore
    colorCalc(gray);
    return calcColor;
  }


  /**
   * @param c can be packed ARGB or a gray in this case
   */
  public final int color(int c, int alpha) {  // ignore
//    if (colorModeDefault) {
//      // bounds checking to make sure the numbers aren't to high or low
//      if (c > 255) c = 255; else if (c < 0) c = 0;
//      if (alpha > 255) alpha = 255; else if (alpha < 0) alpha = 0;
//
//      return ((alpha & 0xff) << 24) | (c << 16) | (c << 8) | c;
//    }
    colorCalc(c, alpha);
    return calcColor;
  }


  /**
   * @param c can be packed ARGB or a gray in this case
   */
  public final int color(int c, float alpha) {  // ignore
//    if (((c & 0xff000000) == 0) && (c <= colorModeX)) {
    colorCalc(c, alpha);
//  } else {
//    colorCalcARGB(c, alpha);
//  }
    return calcColor;
  }


  public final int color(float gray, float alpha) {  // ignore
    colorCalc(gray, alpha);
    return calcColor;
  }


  public final int color(int x, int y, int z) {  // ignore
//    if (colorModeDefault) {
//      // bounds checking to make sure the numbers aren't to high or low
//      if (x > 255) x = 255; else if (x < 0) x = 0;
//      if (y > 255) y = 255; else if (y < 0) y = 0;
//      if (z > 255) z = 255; else if (z < 0) z = 0;
//
//      return 0xff000000 | (x << 16) | (y << 8) | z;
//    }
    colorCalc(x, y, z);
    return calcColor;
  }


  public final int color(float x, float y, float z) {  // ignore
    colorCalc(x, y, z);
    return calcColor;
  }


  public final int color(int x, int y, int z, int a) {  // ignore
//    if (colorModeDefault) {
//      // bounds checking to make sure the numbers aren't to high or low
//      if (a > 255) a = 255; else if (a < 0) a = 0;
//      if (x > 255) x = 255; else if (x < 0) x = 0;
//      if (y > 255) y = 255; else if (y < 0) y = 0;
//      if (z > 255) z = 255; else if (z < 0) z = 0;
//
//      return (a << 24) | (x << 16) | (y << 8) | z;
//    }
    colorCalc(x, y, z, a);
    return calcColor;
  }


  public final int color(float x, float y, float z, float a) {  // ignore
    colorCalc(x, y, z, a);
    return calcColor;
  }



  //////////////////////////////////////////////////////////////

  // COLOR DATATYPE EXTRACTION

  // Vee have veys of making the colors talk.


  /**
   * Extracts the alpha value from a color.
   *
   * @webref color:creating_reading
   * @param what any value of the color datatype
   */
  public final float alpha(int what) {
    float c = (what >> 24) & 0xff;
    if (colorModeA == 255) return c;
    return (c / 255.0f) * colorModeA;
  }


  /**
   * Extracts the red value from a color, scaled to match current <b>colorMode()</b>. This value is always returned as a  float so be careful not to assign it to an int value.<br><br>The red() function is easy to use and undestand, but is slower than another technique. To achieve the same results when working in <b>colorMode(RGB, 255)</b>, but with greater speed, use the &gt;&gt; (right shift) operator with a bit mask. For example, the following two lines of code are equivalent:<br><pre>float r1 = red(myColor);<br>float r2 = myColor &gt;&gt; 16 &amp; 0xFF;</pre>
   *
   * @webref color:creating_reading
   * @param what any value of the color datatype
   *
   * @see PGraphics#green(int)
   * @see PGraphics#blue(int)
   * @see PGraphics#hue(int)
   * @see PGraphics#saturation(int)
   * @see PGraphics#brightness(int)
   * @ref rightshift
   */
  public final float red(int what) {
    float c = (what >> 16) & 0xff;
    if (colorModeDefault) return c;
    return (c / 255.0f) * colorModeX;
  }


  /**
   * Extracts the green value from a color, scaled to match current <b>colorMode()</b>. This value is always returned as a  float so be careful not to assign it to an int value.<br><br>The <b>green()</b> function is easy to use and undestand, but is slower than another technique. To achieve the same results when working in <b>colorMode(RGB, 255)</b>, but with greater speed, use the &gt;&gt; (right shift) operator with a bit mask. For example, the following two lines of code are equivalent:<br><pre>float r1 = green(myColor);<br>float r2 = myColor &gt;&gt; 8 &amp; 0xFF;</pre>
   *
   * @webref color:creating_reading
   * @param what any value of the color datatype
   *
   * @see PGraphics#red(int)
   * @see PGraphics#blue(int)
   * @see PGraphics#hue(int)
   * @see PGraphics#saturation(int)
   * @see PGraphics#brightness(int)
   * @ref rightshift
   */
  public final float green(int what) {
    float c = (what >> 8) & 0xff;
    if (colorModeDefault) return c;
    return (c / 255.0f) * colorModeY;
  }


  /**
   * Extracts the blue value from a color, scaled to match current <b>colorMode()</b>. This value is always returned as a  float so be careful not to assign it to an int value.<br><br>The <b>blue()</b> function is easy to use and undestand, but is slower than another technique. To achieve the same results when working in <b>colorMode(RGB, 255)</b>, but with greater speed, use a bit mask to remove the other color components. For example, the following two lines of code are equivalent:<br><pre>float r1 = blue(myColor);<br>float r2 = myColor &amp; 0xFF;</pre>
   *
   * @webref color:creating_reading
   * @param what any value of the color datatype
   *
   * @see PGraphics#red(int)
   * @see PGraphics#green(int)
   * @see PGraphics#hue(int)
   * @see PGraphics#saturation(int)
   * @see PGraphics#brightness(int)
   */
  public final float blue(int what) {
    float c = (what) & 0xff;
    if (colorModeDefault) return c;
    return (c / 255.0f) * colorModeZ;
  }


  /**
   * Extracts the hue value from a color.
   *
   * @webref color:creating_reading
   * @param what any value of the color datatype
   *
   * @see PGraphics#red(int)
   * @see PGraphics#green(int)
   * @see PGraphics#blue(int)
   * @see PGraphics#saturation(int)
   * @see PGraphics#brightness(int)
   */
  public final float hue(int what) {
    if (what != cacheHsbKey) {
      Color.RGBtoHSB((what >> 16) & 0xff, (what >> 8) & 0xff,
                     what & 0xff, cacheHsbValue);
      cacheHsbKey = what;
    }
    return cacheHsbValue[0] * colorModeX;
  }


  /**
   * Extracts the saturation value from a color.
   *
   * @webref color:creating_reading
   * @param what any value of the color datatype
   *
   * @see PGraphics#red(int)
   * @see PGraphics#green(int)
   * @see PGraphics#blue(int)
   * @see PGraphics#hue(int)
   * @see PGraphics#brightness(int)
   */
  public final float saturation(int what) {
    if (what != cacheHsbKey) {
      Color.RGBtoHSB((what >> 16) & 0xff, (what >> 8) & 0xff,
                     what & 0xff, cacheHsbValue);
      cacheHsbKey = what;
    }
    return cacheHsbValue[1] * colorModeY;
  }


  /**
   * Extracts the brightness value from a color.
   *
   *
   * @webref color:creating_reading
   * @param what any value of the color datatype
   *
   * @see PGraphics#red(int)
   * @see PGraphics#green(int)
   * @see PGraphics#blue(int)
   * @see PGraphics#hue(int)
   * @see PGraphics#saturation(int)
   */
  public final float brightness(int what) {
    if (what != cacheHsbKey) {
      Color.RGBtoHSB((what >> 16) & 0xff, (what >> 8) & 0xff,
                     what & 0xff, cacheHsbValue);
      cacheHsbKey = what;
    }
    return cacheHsbValue[2] * colorModeZ;
  }



  //////////////////////////////////////////////////////////////

  // COLOR DATATYPE INTERPOLATION

  // Against our better judgement.


  /**
   * Calculates a color or colors between two color at a specific increment. The <b>amt</b> parameter is the amount to interpolate between the two values where 0.0 equal to the first point, 0.1 is very near the first point, 0.5 is half-way in between, etc.
   *
   * @webref color:creating_reading
   * @param c1 interpolate from this color
   * @param c2 interpolate to this color
   * @param amt between 0.0 and 1.0
   *
   * @see PGraphics#blendColor(int, int, int)
   * @see PGraphics#color(float, float, float, float)
   */
  public int lerpColor(int c1, int c2, float amt) {
    return lerpColor(c1, c2, amt, colorMode);
  }

  static float[] lerpColorHSB1;
  static float[] lerpColorHSB2;

  /**
   * Interpolate between two colors. Like lerp(), but for the
   * individual color components of a color supplied as an int value.
   */
  static public int lerpColor(int c1, int c2, float amt, int mode) {
    if (mode == RGB) {
      float a1 = ((c1 >> 24) & 0xff);
      float r1 = (c1 >> 16) & 0xff;
      float g1 = (c1 >> 8) & 0xff;
      float b1 = c1 & 0xff;
      float a2 = (c2 >> 24) & 0xff;
      float r2 = (c2 >> 16) & 0xff;
      float g2 = (c2 >> 8) & 0xff;
      float b2 = c2 & 0xff;

      return (((int) (a1 + (a2-a1)*amt) << 24) |
              ((int) (r1 + (r2-r1)*amt) << 16) |
              ((int) (g1 + (g2-g1)*amt) << 8) |
              ((int) (b1 + (b2-b1)*amt)));

    } else if (mode == HSB) {
      if (lerpColorHSB1 == null) {
        lerpColorHSB1 = new float[3];
        lerpColorHSB2 = new float[3];
      }

      float a1 = (c1 >> 24) & 0xff;
      float a2 = (c2 >> 24) & 0xff;
      int alfa = ((int) (a1 + (a2-a1)*amt)) << 24;

      Color.RGBtoHSB((c1 >> 16) & 0xff, (c1 >> 8) & 0xff, c1 & 0xff,
                     lerpColorHSB1);
      Color.RGBtoHSB((c2 >> 16) & 0xff, (c2 >> 8) & 0xff, c2 & 0xff,
                     lerpColorHSB2);

      /* If mode is HSB, this will take the shortest path around the
       * color wheel to find the new color. For instance, red to blue
       * will go red violet blue (backwards in hue space) rather than
       * cycling through ROYGBIV.
       */
      // Disabling rollover (wasn't working anyway) for 0126.
      // Otherwise it makes full spectrum scale impossible for
      // those who might want it...in spite of how despicable
      // a full spectrum scale might be.
      // roll around when 0.9 to 0.1
      // more than 0.5 away means that it should roll in the other direction
      /*
      float h1 = lerpColorHSB1[0];
      float h2 = lerpColorHSB2[0];
      if (Math.abs(h1 - h2) > 0.5f) {
        if (h1 > h2) {
          // i.e. h1 is 0.7, h2 is 0.1
          h2 += 1;
        } else {
          // i.e. h1 is 0.1, h2 is 0.7
          h1 += 1;
        }
      }
      float ho = (PApplet.lerp(lerpColorHSB1[0], lerpColorHSB2[0], amt)) % 1.0f;
      */
      float ho = PApplet.lerp(lerpColorHSB1[0], lerpColorHSB2[0], amt);
      float so = PApplet.lerp(lerpColorHSB1[1], lerpColorHSB2[1], amt);
      float bo = PApplet.lerp(lerpColorHSB1[2], lerpColorHSB2[2], amt);

      return alfa | (Color.HSBtoRGB(ho, so, bo) & 0xFFFFFF);
    }
    return 0;
  }



  //////////////////////////////////////////////////////////////

  // BEGINRAW/ENDRAW


  /**
   * Record individual lines and triangles by echoing them to another renderer.
   */
  public void beginRaw(PGraphics rawGraphics) {  // ignore
    this.raw = rawGraphics;
    rawGraphics.beginDraw();
  }


  public void endRaw() {  // ignore
    if (raw != null) {
      // for 3D, need to flush any geometry that's been stored for sorting
      // (particularly if the ENABLE_DEPTH_SORT hint is set)
      flush();

      // just like beginDraw, this will have to be called because
      // endDraw() will be happening outside of draw()
      raw.endDraw();
      raw.dispose();
      raw = null;
    }
  }



  //////////////////////////////////////////////////////////////

  // WARNINGS and EXCEPTIONS


  static protected HashMap<String, Object> warnings;


  /**
   * Show a renderer error, and keep track of it so that it's only shown once.
   * @param msg the error message (which will be stored for later comparison)
   */
  static public void showWarning(String msg) {  // ignore
    if (warnings == null) {
      warnings = new HashMap<String, Object>();
    }
    if (!warnings.containsKey(msg)) {
      System.err.println(msg);
      warnings.put(msg, new Object());
    }
  }


  /**
   * Display a warning that the specified method is only available with 3D.
   * @param method The method name (no parentheses)
   */
  static public void showDepthWarning(String method) {
    showWarning(method + "() can only be used with a renderer that " +
                "supports 3D, such as P3D or OPENGL.");
  }


  /**
   * Display a warning that the specified method that takes x, y, z parameters
   * can only be used with x and y parameters in this renderer.
   * @param method The method name (no parentheses)
   */
  static public void showDepthWarningXYZ(String method) {
    showWarning(method + "() with x, y, and z coordinates " +
                "can only be used with a renderer that " +
                "supports 3D, such as P3D or OPENGL. " +
                "Use a version without a z-coordinate instead.");
  }


  /**
   * Display a warning that the specified method is simply unavailable.
   */
  static public void showMethodWarning(String method) {
    showWarning(method + "() is not available with this renderer.");
  }


  /**
   * Error that a particular variation of a method is unavailable (even though
   * other variations are). For instance, if vertex(x, y, u, v) is not
   * available, but vertex(x, y) is just fine.
   */
  static public void showVariationWarning(String str) {
    showWarning(str + " is not available with this renderer.");
  }


  /**
   * Display a warning that the specified method is not implemented, meaning
   * that it could be either a completely missing function, although other
   * variations of it may still work properly.
   */
  static public void showMissingWarning(String method) {
    showWarning(method + "(), or this particular variation of it, " +
                "is not available with this renderer.");
  }


  /**
   * Show an renderer-related exception that halts the program. Currently just
   * wraps the message as a RuntimeException and throws it, but might do
   * something more specific might be used in the future.
   */
  static public void showException(String msg) {  // ignore
    throw new RuntimeException(msg);
  }


  /**
   * Same as below, but defaults to a 12 point font, just as MacWrite intended.
   */
  protected void defaultFontOrDeath(String method) {
    defaultFontOrDeath(method, 12);
  }


  /**
   * First try to create a default font, but if that's not possible, throw
   * an exception that halts the program because textFont() has not been used
   * prior to the specified method.
   */
  protected void defaultFontOrDeath(String method, float size) {
    if (parent != null) {
      textFont = parent.createDefaultFont(size);
    } else {
      throw new RuntimeException("Use textFont() before " + method + "()");
    }
  }



  //////////////////////////////////////////////////////////////

  // RENDERER SUPPORT QUERIES


  /**
   * Return true if this renderer should be drawn to the screen. Defaults to
   * returning true, since nearly all renderers are on-screen beasts. But can
   * be overridden for subclasses like PDF so that a window doesn't open up.
   * <br/> <br/>
   * A better name? showFrame, displayable, isVisible, visible, shouldDisplay,
   * what to call this?
   */
  public boolean displayable() {
    return true;
  }


  /**
   * Return true if this renderer supports 2D drawing. Defaults to true.
   */
  public boolean is2D() {
    return true;
  }


  /**
   * Return true if this renderer supports 3D drawing. Defaults to false.
   */
  public boolean is3D() {
    return false;
  }

  //////////////////////////////////////////////////////////////

  // New API:

  protected String[] getSupportedShapeFormats() {
    showMissingWarning("getSupportedShapeFormats");
    return null;
  }


  protected PShape loadShape(String filename, Object params) {
    showMissingWarning("loadShape");
    return null;
  }


  protected PShape createShape(int size, Object params) {
    showMissingWarning("createShape");
    return null;
  }


  public void screenBlend(int mode) {
    showMissingWarning("screenBlend");
  }


  public void textureBlend(int mode) {
    showMissingWarning("textureBlend");
  }


  public PShape beginRecord() { // ignore
    showMissingWarning("beginRecord");
    return null;
  }


  public void endRecord() {  // ignore
    showMissingWarning("endRecord");
  }


  public boolean isRecording() {
    showMissingWarning("isRecording");
    return false;
  }


  public void mergeShapes(boolean val) {
    showMissingWarning("mergeShapes");
  }


  public void shapeName(String name) {
    showMissingWarning("shapeName");
  }


  public void autoNormal(boolean auto) {
    this.autoNormal = auto;
  }


  public void matrixMode(int mode) {
    showMissingWarning("matrixMode");
  }


  public void beginText() {
    showMissingWarning("beginText");
  }


  public void endText() {
    showMissingWarning("endText");
  }


  public void texture(PImage... images) {
    showMissingWarning("texture");
  }

  public void vertex(float... values) {
    showMissingWarning("vertex");
  }
}
