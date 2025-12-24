/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package org.jogamp.java3d.examples.fps_counter;

import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.examples.java3dexamples.R;
import org.jogamp.java3d.utils.geometry.ColorCube;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;

import jogamp.newt.driver.android.NewtBaseActivity;

/**
 * This program demonstrates the use of the frames per second counter.
 * The program displays a rotating cube and sets up the FPSCounter to compute
 * the frame rate. The FPSCounter is set up with default values:
 * 	- run indefinitely
 * 	- 2 sec. warmup time
 * 	- display average frame rate every fifth sampling interval.
 * The default values can be changed through the command line
 * arguments. Use FPSCounterDemo1 -h for help on the various arguments.
 */
public class FPSCounterDemo extends NewtBaseActivity {

    private SimpleUniverse univ = null;
    private BranchGroup scene = null;
    private FPSCounter fpsCounter = new FPSCounter(this);

	private GLWindowOverLay fpsPanel;
	private TextView fpsTextView;
    
    BranchGroup createSceneGraph() {
		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();

		// Create the TransformGroup node and initialize it to the
		// identity. Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at run time. Add it to
		// the root of the subgraph.
		TransformGroup objTrans = new TransformGroup();
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objRoot.addChild(objTrans);

		// Create a simple Shape3D node; add it to the scene graph.
		objTrans.addChild(new ColorCube(0.4));

		// Create a new Behavior object that will perform the
		// desired operation on the specified transform and add
		// it into the scene graph.
		Transform3D yAxis = new Transform3D();
		Alpha rotationAlpha = new Alpha(-1, 4000);

		RotationInterpolator rotator =
			new RotationInterpolator(rotationAlpha, objTrans, yAxis,
					 0.0f, (float) Math.PI*2.0f);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0),
							100.0);
		rotator.setSchedulingBounds(bounds);
		objRoot.addChild(rotator);

		// Create the Framecounter behavior
		fpsCounter.setSchedulingBounds(bounds);
		objRoot.addChild(fpsCounter);

		fpsCounter.setOutputView(fpsTextView);

		return objRoot;
    }

    private Canvas3D createUniverse() {

		// Create a Canvas3D using the preferred configuration
		Canvas3D c = new Canvas3D();

		// Create simple universe with view branch
		univ = new SimpleUniverse(c);

		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		univ.getViewingPlatform().setNominalViewingTransform();

		return c;
    }

	// ----------------------------------------------------------------

	private Canvas3D c = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SimpleShaderAppearance.setVersionES300();
        
        // Create Canvas3D and SimpleUniverse; add canvas to drawing panel
        c = createUniverse();

        /*JOptionPane.showMessageDialog(this,
                ("This program measures the number of frames rendered per second.\n" +
                "Note that the frame rate is limited by the refresh rate of the monitor.\n" +
                "To get the true frame rate you need to disable vertical retrace.\n\n" +
                "On Windows(tm) you do this through the Control Panel.\n\n" +
                "On Solaris set the environment variable OGL_NO_VBLANK"),
                "Frame Counter",
                JOptionPane.INFORMATION_MESSAGE);)*/

		fpsPanel = new GLWindowOverLay(this, this.getWindow().getDecorView(), R.layout.fpspanel, Gravity.RIGHT | Gravity.TOP, true, 0, 0);
		fpsTextView = (TextView) fpsPanel.getButton(R.id.fpsTextView);

		// Create the content branch and add it to the universe
		scene = createSceneGraph();
		univ.addBranchGraph(scene);

		// make the gl window the content of this app
		this.setContentView(this.getWindow(), c.getGLWindow());

		c.getGLWindow().addGLEventListener(glWindowInitListener);
	}


	GLEventListener glWindowInitListener = new GLEventListener() {
		@Override
		public void init(@SuppressWarnings("unused") final GLAutoDrawable drawable) {
		}

		@Override
		public void reshape(final GLAutoDrawable drawable, final int x, final int y,
							final int w, final int h) {
		}

		@Override
		public void display(final GLAutoDrawable drawable) {
			// showing the nav panel can only be done on the UI thread
			runOnUiThread(new Runnable() {
				public void run() {
					fpsPanel.showTooltip();
				}
			});
		}

		@Override
		public void dispose(final GLAutoDrawable drawable) {
		}
	};

	// the 4 methods below are life cycle management to keep the app stable and well behaved
	@Override
	public void onResume() {
		c.getGLWindow().setVisible(true);
		c.startRenderer();
		super.onResume();
	}

	@Override
	public void onPause() {
		c.stopRenderer();
		c.removeNotify();
		c.getGLWindow().setVisible(false);
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		c.stopRenderer();
		c.removeNotify();
		c.getGLWindow().destroy();
		try {
			if (univ != null) {
				univ.cleanup();
				univ = null;
			}
			super.onDestroy();
		} catch (Exception e) {
			//ignore as we are done
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

    /** Parses the commandline for the various switches to set the FPSCounter
     * variables.
     * All arguments are of the form <i>-name value</i>.
     * All -name arguments can be shortened to one character. All the value
     * arguments take a number. The arguments accepted are :
     * <ul>
     * <li>warmupTime : Specifies amount of time the FPSCounter should wait
     * for the HotSpot<sup><font size="-2">TM</font></sup> VM to perform
     * initial optimizations. Specified in milliseconds<br>
     * <li>loopCount  : Specifies the number of sampling intervals over which
     * the FPSCounter should calculate the aggregate and average frame rate.
     * Specified as a count. <br>
     * <li>maxLoops   : Specifies that the FPSCounter should run for only
     * these many sampling intervals. Specified as number. If this argument
     * is not specified, the FPSCounter runs indefinitely. <br>
     * <li>help	   : Prints the accepted arguments. <br>
     * </ul>
     */
   private void parseArgs(String args[]) {
      for(int i = 0; i < args.length; i++) {
		  if(args[i].startsWith("-")) {
			  if(args[i].startsWith("w", 1)) {
			  i++;
			  System.out.println("Warmup time : " + args[i]);
			  int w = new Integer(args[i]).intValue();
			  fpsCounter.setWarmupTime(w);
			  }
			  else if(args[i].startsWith("l", 1)) {
			  i++;
			  System.out.println("Loop count : " + args[i]);
			  int l = new Integer(args[i]).intValue();
			  fpsCounter.setLoopCount(l);
			  }
			  else if(args[i].startsWith("m", 1)) {
			  i++;
			  System.out.println("Max Loop Count : " + args[i]);
			  int m = new Integer(args[i]).intValue();
			  fpsCounter.setMaxLoops(m);
			  }
			  else if(args[i].startsWith("h", 1)) {
			  System.out.println("Usage : FPSCounterDemo [-name value]\n" +
				   "All arguments are of the form: -name value. All -name arguments can be\n" +
				   "shortened to one character. All the value arguments take a number. The\n" +
				   "arguments accepted are:\n\n" +
				   "    -warmupTime : Specifies amount of time the FPSCounter should wait\n" +
				   "        for the HotSpot(tm) VM to perform initial\n" +
				   "        optimizations. Specified in milliseconds\n\n" +
				   "    -loopCount : Specifies the number of sampling intervals over which\n" +
				   "        the FPSCounter should calculate the aggregate and average\n" +
				   "        frame rate. Specified as a count\n\n" +
				   "    -maxLoops : Specifies that the FPSCounter should run for only these\n" +
				   "        many sampling intervals. Specified as number. If this argument\n" +
				   "        is not specified, the FPSCounter runs indefinitely.\n\n" +
				   "    -help : Prints this message.");
			  }
		  }
      }
   }

}
