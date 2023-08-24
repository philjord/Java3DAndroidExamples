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

package org.jogamp.java3d.examples.depth_func;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.examples.java3dhelloworld.R;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;

import java.io.PrintStream;

import jogamp.newt.driver.android.NewtBaseFragmentActivity;


/**
 *The goal of that example is to show the use of different ZBuffer comparison modes.
 */
public class DepthFuncTest extends NewtBaseFragmentActivity {
    
    RenderFrame rf;
    @Override
    public void onCreate(final Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.depthfuncmain);

        SimpleShaderAppearance.setVersionES300();

        // create a RenderFrame Fragment and put it into the frame layout waiting for it
        rf = new RenderFrame( this );

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.java3dSpace, rf);
        fragmentTransaction.commit();

        normalComboBox = findViewById(R.id.normalComboBox);
        wfCheckBox = findViewById(R.id.wfCheckBox);
        shadedComboBox = findViewById(R.id.shadedComboBox);
        shadedCheckBox = findViewById(R.id.shadedCheckBox);
        rotatingComboBox = findViewById(R.id.rotatingComboBox);

        normalComboBox.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] { "ALWAYS", "NEVER", "EQUAL", "NOT_EQUAL", "LESS", "LESS_OR_EQUAL", "GREATER", "GREATER_OR_EQUAL" })
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                return getDropDownView(position, convertView, parent);
            }
            @Override
            public View getDropDownView (int position, View convertView, ViewGroup parent)
            {
                TextView ret = new TextView(getContext());
                ret.setText((String)normalComboBox.getItemAtPosition(position));
                return ret;
            }
        });
        normalComboBox.setSelection(6);
        normalComboBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) {  }//odd
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                normalComboBoxActionPerformed();
            }
        });

        wfCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                wfCheckBoxActionPerformed();
            }
        });


        shadedComboBox.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] { "ALWAYS", "NEVER", "EQUAL", "NOT_EQUAL", "LESS", "LESS_OR_EQUAL", "GREATER", "GREATER_OR_EQUAL" })
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                return getDropDownView(position, convertView, parent);
            }
            @Override
            public View getDropDownView (int position, View convertView, ViewGroup parent)
            {
                TextView ret = new TextView(getContext());
                ret.setText((String)shadedComboBox.getItemAtPosition(position));
                return ret;
            }
        });
        shadedComboBox.setSelection(4);
        shadedComboBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) {  }//odd
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                shadedComboBoxActionPerformed();
            }
        });

        shadedCheckBox.setSelected(true);

        shadedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                shadedCheckBoxActionPerformed();
            }
        });

        rotatingComboBox.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] { "CLEAR", "AND", "AND_REVERSE", "COPY", "AND_INVERTED", "NOOP", "XOR", "OR", "NOR", "EQUIV", "INVERT", "OR_REVERSE", "COPY_INVERTED", "OR_INVERTED", "NAND", "SET" })
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                return getDropDownView(position, convertView, parent);
            }
            @Override
            public View getDropDownView (int position, View convertView, ViewGroup parent)
            {
                TextView ret = new TextView(getContext());
                ret.setText((String)rotatingComboBox.getItemAtPosition(position));
                return ret;
            }
        });
        rotatingComboBox.setSelection(3);
        rotatingComboBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) {  }//odd
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                rotatingComboBoxActionPerformed();
            }
        });
    }


    private void rotatingComboBoxActionPerformed()
    {
        String selectedItem = rotatingComboBox.getSelectedItem().toString();  // how to avoid a cast and all that goes with it. (lazyness)
        int mode = RenderingAttributes.ROP_COPY;
        if ( "CLEAR".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_CLEAR;
        }
        else if ( "AND".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_AND;
        }
        else if ( "AND_REVERSE".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_AND_REVERSE;
        }
        else if ( "COPY".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_COPY;
        }
        else if ( "AND_INVERTED".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_AND_INVERTED;
        }
        else if ( "NOOP".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_NOOP;
        }
        else if ( "XOR".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_XOR;
        }
        else if ( "OR".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_OR;
        }
        else if ( "NOR".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_NOR;
        }
        else if ( "EQUIV".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_EQUIV;
        }
        else if ( "INVERT".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_INVERT;
        }
        else if ( "OR_REVERSE".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_OR_REVERSE;
        }
        else if ( "COPY_INVERTED".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_COPY_INVERTED;
        }
        else if ( "OR_INVERTED".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_OR_INVERTED;
        }
        else if ( "NAND".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_NAND;
        }
        else if ( "SET".equals(selectedItem) )
        {
            mode = RenderingAttributes.ROP_SET;
        }
        else
        {
            System.out.println("oops. wrong mode in ROP combo: "+selectedItem);
        }
        rf.setRotatingObjectROPMode( mode );
    }

    private void shadedCheckBoxActionPerformed() {
        rf.setStaticObjectDBWriteStatus( shadedCheckBox.isSelected() );
    }

    private void wfCheckBoxActionPerformed() {
        rf.setStaticWFObjectDBWriteStatus( wfCheckBox.isSelected() );
    }

    private void shadedComboBoxActionPerformed() {
        int func = RenderingAttributes.LESS_OR_EQUAL;
        String selectedItem = shadedComboBox.getSelectedItem().toString();  // how to avoid a cast and all that goes with it. (lazyness)
        rf.setStaticObjectTestFunc( getID( selectedItem ) );
    }

    private void normalComboBoxActionPerformed() {
        int func = RenderingAttributes.LESS_OR_EQUAL;
        String selectedItem = normalComboBox.getSelectedItem().toString();  // how to avoid a cast and all that goes with it. (lazyness)
        rf.setStaticWFObjectTestFunc( getID( selectedItem ) );
    }

    int getID( String selectedItem ) 
    {
      int func = RenderingAttributes.LESS_OR_EQUAL;
      if ( "LESS_OR_EQUAL".equals(selectedItem) )
      {
          func = RenderingAttributes.LESS_OR_EQUAL;
      }
      else if ( "NEVER".equals(selectedItem) )
      {
          func = RenderingAttributes.NEVER;
      }
      else if ( "ALWAYS".equals(selectedItem) )
      {
          func = RenderingAttributes.ALWAYS;
      }
      else if ( "GREATER".equals(selectedItem) )
      {
          func = RenderingAttributes.GREATER;
      }
      else if ( "GREATER_OR_EQUAL".equals(selectedItem) )
      {
          func = RenderingAttributes.GREATER_OR_EQUAL;
      }
      else if ( "LESS".equals(selectedItem) )
      {
          func = RenderingAttributes.LESS;
      }
      else if ( "EQUAL".equals(selectedItem) )
      {
          func = RenderingAttributes.EQUAL;
      }
      else if ( "NOT_EQUAL".equals(selectedItem) )
      {
          func = RenderingAttributes.NOT_EQUAL;
      }
      return func;
    }
    

    
    private Spinner normalComboBox;
    private Spinner rotatingComboBox;
    private CheckBox shadedCheckBox;
    private Spinner shadedComboBox;
    private CheckBox wfCheckBox;

}
