package org.twak.tweed.gen;

import java.io.File;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.vecmath.Point3d;

import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.twak.siteplan.jme.Jme3z;
import org.twak.tweed.EventMoveHandle;
import org.twak.tweed.Tweed;
import org.twak.tweed.TweedSettings;
import org.twak.utils.Filez;
import org.twak.utils.geom.Graph3D;
import org.twak.viewTrace.GMLReader;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;

public class GraphGen extends Gen implements ICanSave {

	public File source;
	
	public Graph3D graph;
	
	public GraphGen() {}
	public GraphGen( File source, Tweed tweed ) {
		super( Filez.stripExtn( source.getName( )), tweed);
		this.source = source;

		CoordinateReferenceSystem crss = Tweed.kludgeCMS.get( TweedSettings.settings.gmlCoordSystem );		
		graph = GMLReader.readGMLGraph( Tweed.toWorkspace( source ), DefaultGeocentricCRS.CARTESIAN, crss );
		
		graph.transform ( TweedSettings.settings.toOrigin );		
	}
	
	@Override
	public void calculate() {

		super.calculate();

		for ( Spatial s : gNode.getChildren() )
			s.removeFromParent();

		Random randy = new Random();
		
		Material lineMat = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );
		lineMat.setColor( "Diffuse", Jme3z.toJme( color ) );
		lineMat.setColor( "Ambient", Jme3z.toJme( color ) );
		lineMat.setBoolean( "UseMaterialColors", true );
		lineMat.getAdditionalRenderState().setLineWidth( 5f );
				
		for ( Point3d p1 : graph.getAllDiscrete() ) {

			Box box1 = new Box( 2f, 2f, 2f );
			Geometry geom = new Geometry( "Box", box1 );

			geom.setUserData( EventMoveHandle.class.getSimpleName(), new Object[] { new EventMoveHandle() {
				@Override
				public void posChanged() {
					
					List<Point3d> to = graph.get( p1 );
					graph.remove( p1 );
					p1.set( Jme3z.from( geom.getLocalTranslation() ) );
					graph.putAll( p1, to );
					System.out.println(p1);
					calculate();
				}
			} } );

			ColorRGBA col = new ColorRGBA( color.getRed() * ( 0.2f + randy.nextFloat() * 0.8f ) / 255f, color.getGreen() * ( 0.2f + randy.nextFloat() * 0.8f ) / 255f, color.getBlue() * ( 0.2f + randy.nextFloat() * 0.8f ) / 255f, 1f );
			Material mat = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );
			
			mat.setColor( "Diffuse", col );
			mat.setColor( "Ambient", col );
			mat.setBoolean( "UseMaterialColors", true );

			geom.setMaterial( mat );
			geom.setLocalTranslation( (float) p1.x, (float) p1.y, (float) p1.z );
			gNode.attachChild( geom );
			
			for (Point3d p2 : graph.get( p1 )) {
				Line line = new Line ( Jme3z.to( p1 ), Jme3z.to( p2) );
				Geometry lg = new Geometry("line", line);
				lg.setMaterial( lineMat );
				gNode.attachChild( lg );

				Point3d p3 = new Point3d((p2.x+p1.x)/2, (p2.y+p1.y)/2, (p2.z+p1.z)/2);
				Box box2 = new Box(2f, 2f, 2f);
				Geometry bg = new Geometry("box", box2);
//				bg.setUserData(EventMoveHandle.class.getSimpleName(), new Object[] {
//
//				});
				ColorRGBA col2 = new ColorRGBA( 1f, 1f , 0, 1f );
				Material mat2 = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );

				mat2.setColor( "Diffuse", col2 );
				mat2.setColor( "Ambient", col2 );
				mat2.setBoolean( "UseMaterialColors", true );

				bg.setMaterial(mat2);
				bg.setLocalTranslation( (float) p3.x, (float) p3.y, (float) p3.z );
				gNode.attachChild( bg );
			}
		}
	}


	@Override
	public JComponent getUI() {
		return new JLabel("Graph");
	}

}
