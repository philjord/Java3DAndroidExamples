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

package org.jogamp.java3d.examples.lightwave;

import android.os.Bundle;
import android.view.KeyEvent;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.View;
import org.jogamp.java3d.loaders.Loader;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.lw3d.Lw3dLoader;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Matrix4d;

import jogamp.newt.driver.android.NewtBaseActivity;


/**
 * This class loads in a Lightwave3D file and displays it in an applet
 * window.  The application is fairly basic; a more complete version
 * of a Lightwave 3D loader might incorporate features such as
 * settable clip plane distances and animated views (these are both
 * possible with the current Lightwave 3D loader, they just need to
 * be implemented in the application).
 */

/**
 * PJ doesn't work as the URL opening code in LwsObject which reads as
 * URL.getContent()
 *
 * need to be swapped for this possibly
 * connection = URL.openConnection();
 * connection.connect(); // <-- does it work if you add this line?
 * Object response = connection.getContent();
 */
public class LightWaveViewer extends NewtBaseActivity {

    private java.net.URL filename;
	private SimpleUniverse univ = null;
	private BranchGroup scene = null;

	// ----------------------------------------------------------------

	private Canvas3D c = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SimpleShaderAppearance.setVersionES300();

		if (filename == null) {
			String lwsfile = "/resources/geometry/ballcone.lws";
			filename = getClass().getResource(lwsfile);
			if (filename == null)
			{
				System.err.println(lwsfile + " not found");
				System.exit(1);
			}
		}

		// Construct the Lw3d loader and load the file
		Loader lw3dLoader = new Lw3dLoader(Loader.LOAD_ALL);
		Scene loaderScene = null;
		try {
			loaderScene = lw3dLoader.load(filename);
		}
		catch (Exception e) {
			e.printStackTrace();
				System.exit(1);
		}

		Canvas3D c = new Canvas3D();

		// Create a basic universe setup and the root of our scene
		univ = new SimpleUniverse(c);
		BranchGroup sceneRoot = new BranchGroup();

		// Change the back clip distance; the default is small for
		// some lw3d worlds
		View theView = univ.getViewer().getView();
		theView.setBackClipDistance(50000f);

		// Now add the scene graph defined in the lw3d file
		if (loaderScene.getSceneGroup() != null) {
			// Instead of using the default view location (which may be
			// completely bogus for the particular file you're loading),
			// let's use the initial view from the file.  We can get
			// this by getting the  view groups from the scene (there's
			// only one for Lightwave 3D), then using the inverse of the
			// transform on that view as the transform for the entire scene.

			// First, get the view groups (shouldn't be null unless there
			// was something wrong in the load
			TransformGroup viewGroups[] = loaderScene.getViewGroups();

			// Get the Transform3D from the view and invert it
			Transform3D t = new Transform3D();
			viewGroups[0].getTransform(t);
			Matrix4d m = new Matrix4d();
			t.get(m);
			m.invert();
			t.set(m);

			// Now we've got the transform we want.  Create an
			// appropriate TransformGroup and parent the scene to it.
			// Then insert the new group into the main BranchGroup.
			TransformGroup sceneTransform = new TransformGroup(t);
			sceneTransform.addChild(loaderScene.getSceneGroup());
			sceneRoot.addChild(sceneTransform);
		}

		// Make the scene graph live by inserting the root into the universe
		univ.addBranchGraph(sceneRoot);

		// make the gl window the content of this app
		this.setContentView(this.getWindow(), c.getGLWindow());
	}

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
}




