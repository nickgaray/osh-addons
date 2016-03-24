/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
The Initial Developer is Botts Innovative Research Inc. Portions created by the Initial
Developer are Copyright (C) 2016 the Initial Developer. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/


package org.sensorhub.impl.sensor.videocam;

/**
 * <p>
 * Implementation of a helper class to support all video cameras with or without 
 * Pan-Tilt-Zoom (PTZ) control
 * </p>
 *
 * @author Mike Botts <mike.botts@botts-inc.com>
 * @since March 2016
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.swe.v20.AllowedTokens;
import net.opengis.swe.v20.AllowedValues;
import net.opengis.swe.v20.Count;
import net.opengis.swe.v20.DataChoice;
import net.opengis.swe.v20.DataRecord;
import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.Quantity;
import net.opengis.swe.v20.Text;
import net.opengis.swe.v20.Time;

import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;

public class VideoCamHelper extends SWEHelper
{
	// System Info  //
	
	String cameraBrand = " ";
	String cameraModel = " ";
	String serialNumber = " ";
	
	
	// Pan-Tilt-Zoom Properties (output and tasking parameters) //
	
//	boolean ptzEnabled = false;
//	
//    double minPan = -180.0;
//    double maxPan = 180.0;
//    //double pan = 0.0;
//    double panSpeed = 1.0;
//    
//    double minTilt = 0.0;
//    double maxTilt = 90.0;
//    //double tilt = 0.0;
//    double tiltSpeed = 1.0;
//    
//    double minZoom = 0.0;
//    double maxZoom = 0.0;
//    //double zoom = 0.0;
//    double zoomSpeed = 1.0;
    
    
    //   Camera Settings   //
    
    // InfraRed
//    boolean irEnabled = false;  // Can you set camera to IR mode
//    //boolean irOn = false;		// Is camera currently in IR mode
//    
//    //Color Settings
//    double minBrightness;
//    double maxBrightness;
//    //double brightness;
//    
//    double minContrast;
//    double maxContrast;
//    //double contrast;
//    
//    double minHue;
//    double maxHue;
//    //double hue;
//    
//    double minSaturation;
//    double maxSaturation;
//    //double saturation;
//
//    // Video 
//    boolean videoEnabled = true;
//    double videoHeight;	// Height of Video Frame (in contrast to snapshot image height)
//    double videoWidth;	// Width of Video Frame
//    double videoFPS;    // Frames per Second
//    String videoCompression;  // e.g. MPEG4, MPEG2, MPEG1, MJPG, H263, H264
//  
	
    
    public DataRecord getPtzOutput(String name, double minPan, double maxPan, double minTilt, double maxTilt, double minZoom, double maxZoom)
    {
    	
        // Build SWE Common Data structure for PTZ Output values
        // Settings output includes time, pan, tilt, zoom
 
        DataRecord settingsDataStruct = newDataRecord(4);
        settingsDataStruct.setName(name);

        // time needs to be in UTC !!!
        // either set camera and convert
        Time t = this.newTimeStampIsoUTC();        
        settingsDataStruct.addComponent("time", t);

        AllowedValues constraints;
        
        // TODO: set localReferenceFrame for Z to be the pan axis into camera, 
        Quantity q = this.newQuantity("http://sensorml.com/ont/swe/property/Pan", "Pan", "Gimbal rotation (usually horizontal)", "deg", DataType.FLOAT);
        constraints = newAllowedValues();
        constraints.addInterval(new double[] {minPan, maxPan});
        q.setConstraint(constraints);
        settingsDataStruct.addComponent("pan", q);

        q = this.newQuantity("http://sensorml.com/ont/swe/property/Tilt", "Tilt", "Gimbal rotation (usually up-down)", "deg", DataType.FLOAT);
        q.getUom().setCode("deg");
        q.setDefinition("http://sensorml.com/ont/swe/property/Tilt");
        constraints = newAllowedValues();
        constraints.addInterval(new double[] {minTilt, maxTilt});
        q.setConstraint(constraints);
        q.setLabel("Tilt");
        settingsDataStruct.addComponent("tilt", q);

        // NOTE: zoom factor range needs to be converted between 1-100 in driver
        q = this.newQuantity("http://sensorml.com/ont/swe/property/ZoomFactor", "Zoom Factor", "Percentage of zoom between 1 and 100 percent", "%", DataType.FLOAT);
        constraints = newAllowedValues();
        constraints.addInterval(new double[] {minZoom, maxZoom});
        q.setConstraint(constraints);
        settingsDataStruct.addComponent("zoomFactor", q);

    	return settingsDataStruct;
    }

    
    public DataChoice getPtzTaskParameters(String name, double minPan, double maxPan, double minTilt, double maxTilt, double minZoom, double maxZoom, List <String> presetPosNames)
    {
    	
        // NOTE: commands are individual and supported using DataChoice
       	
        // PTZ Command Options will consist of DataChoice with items:
        // pan, tilt, zoom, relPan, relTilt, relZoom, presetPos (?)

        DataChoice commandData = this.newDataChoice();
        commandData.setName(name);

       AllowedValues constraints;
       
       // Pan
       Quantity q = newQuantity(DataType.FLOAT);
       q.getUom().setCode("deg");
       q.setDefinition("http://sensorml.com/ont/swe/property/Pan");
       constraints = newAllowedValues();
       constraints.addInterval(new double[] {minPan, maxPan});
       q.setConstraint(constraints);
       q.setLabel("Pan");
       commandData.addItem("pan",q);

       // Tilt
       q = newQuantity(DataType.FLOAT);
       q.getUom().setCode("deg");
       q.setDefinition("http://sensorml.com/ont/swe/property/Tilt");
       constraints = newAllowedValues();
       constraints.addInterval(new double[] {minTilt, maxTilt});
       q.setConstraint(constraints);
       q.setLabel("Tilt");
       commandData.addItem("tilt", q);

       // Zoom Factor
       Count c = newCount();
       c.setDefinition("http://sensorml.com/ont/swe/property/AxisZoomFactor");
       constraints = newAllowedValues();
       constraints.addInterval(new double[] {minZoom, maxZoom});
       c.setConstraint(constraints);
       c.setLabel("Zoom Factor");
       commandData.addItem("zoom", c);
        
       // Relative Pan
       q = newQuantity(DataType.FLOAT);
       q.getUom().setCode("deg");
       q.setDefinition("http://sensorml.com/ont/swe/property/relativePan");
       //constraints = fac.newAllowedValues();
       //constraints.addInterval(new double[] {minPan, maxPan});
       //relPan.setConstraint(constraints);
       q.setLabel("Relative Pan");
       commandData.addItem("rpan", q);

       // Relative Tilt
       q = newQuantity(DataType.FLOAT);
       q.getUom().setCode("deg");
       q.setDefinition("http://sensorml.com/ont/swe/property/relativeTilt");
       //constraints = fac.newAllowedValues();
       //constraints.addInterval(new double[] {minTilt, maxTilt});
       //relTilt.setConstraint(constraints);
       q.setLabel("Relative Tilt");
       commandData.addItem("rtilt", q);

       // Relative Zoom
       c = newCount();
       c.setDefinition("http://sensorml.com/ont/swe/property/relativeAxisZoomFactor");
       //constraints = fac.newAllowedValues();
       //constraints.addInterval(new double[] {0, maxZoom});
       //relZoom.setConstraint(constraints);
       c.setLabel("Relative Zoom Factor");
       commandData.addItem("rzoom", c);

       // PTZ preset positions
       Text preset = newText();
       preset.setDefinition("http://sensorml.com/ont/swe/property/cameraPresetPositionName");
       preset.setLabel("Preset Camera Position");
       AllowedTokens presetNames = newAllowedTokens();     
       
       for (String position : presetPosNames)
    	   presetNames.addValue(position);
       
       preset.setConstraint(presetNames);
       //commandData.addItem("gotoserverpresetname",preset);  // this was set to an Axis specific name
       commandData.addItem("gotoPresetPosition",preset);

       return commandData;
    	
    }
    
    
    
}
