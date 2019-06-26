package org.twak.tweed.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.vecmath.Point3d;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.twak.siteplan.jme.Jme3z;
import org.twak.tweed.EventMoveHandle;
import org.twak.tweed.Tweed;
import org.twak.tweed.TweedSettings;
import org.twak.utils.Filez;
import org.twak.utils.collections.Arrayz;
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

				// midpoint
				Point3d p3 = new Point3d((p2.x+p1.x)/2, (p2.y+p1.y)/2, (p2.z+p1.z)/2);
				Box box2 = new Box(2f, 2f, 2f);
				Geometry bg = new Geometry("box", box2);

				ColorRGBA col2 = new ColorRGBA( 1f, 1f , 0, 1f );
				Material mat2 = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );

				mat2.setColor( "Diffuse", col2 );
				mat2.setColor( "Ambient", col2 );
				mat2.setBoolean( "UseMaterialColors", true );

				bg.setMaterial(mat2);
				bg.setLocalTranslation( (float) p3.x, (float) p3.y, (float) p3.z );
				gNode.attachChild( bg );

				// corners
				Point3d vector = new Point3d(-(p2.z-p1.z), p2.y-p1.y, p2.x-p1.x);
				double magnitude = Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.y, 2) + Math.pow(vector.z, 2));
				Point3d v = new Point3d(vector.x*(10/magnitude), vector.y*(10/magnitude), vector.z*(10/magnitude));

				Point3d c1 = new Point3d((p1.x+v.x), (p1.y+v.y), (p1.z+v.z));
				Point3d c2 = new Point3d((p1.x-v.x), (p1.y-v.y), (p1.z-v.z));
				Point3d c3 = new Point3d((p2.x+v.x), (p2.y+v.y), (p2.z+v.z));
				Point3d c4 = new Point3d((p2.x-v.x), (p2.y-v.y), (p2.z-v.z));

				Box b1 = new Box(1f, 1f, 1f);
				Box b2 = new Box(1f, 1f, 1f);
				Box b3 = new Box(1f, 1f, 1f);
				Box b4 = new Box(1f, 1f, 1f);
				Geometry g1 = new Geometry("box", b1);
				Geometry g2 = new Geometry("box", b2);
				Geometry g3 = new Geometry("box", b3);
				Geometry g4 = new Geometry("box", b4);

				ColorRGBA col3 = new ColorRGBA( 0, 1f , 1f, 1f );
				Material mat3 = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );

				mat3.setColor( "Diffuse", col3 );
				mat3.setColor( "Ambient", col3 );
				mat3.setBoolean( "UseMaterialColors", true );

				g1.setMaterial(mat3);
				g2.setMaterial(mat3);
				g3.setMaterial(mat3);
				g4.setMaterial(mat3);
				g1.setLocalTranslation( (float) c1.x, (float) c1.y, (float) c1.z );
				g2.setLocalTranslation( (float) c2.x, (float) c2.y, (float) c2.z );
				g3.setLocalTranslation( (float) c3.x, (float) c3.y, (float) c3.z );
				g4.setLocalTranslation( (float) c4.x, (float) c4.y, (float) c4.z );
				gNode.attachChild( g1 );
				gNode.attachChild( g2 );
				gNode.attachChild( g3 );
				gNode.attachChild( g4 );

				Mesh m = new Mesh();
				m.setMode(Mesh.Mode.Lines);

				List<Float> coords = new ArrayList();
				//List<Integer> inds = new ArrayList();

				//inds.add( inds.size() );

				coords.add( (float) c1.x );
				coords.add( (float) c1.y );
				coords.add( (float) c1.z );
				coords.add( (float) c2.x );
				coords.add( (float) c2.y );
				coords.add( (float) c2.z );
				coords.add( (float) c3.x );
				coords.add( (float) c3.y );
				coords.add( (float) c3.z );
				coords.add( (float) c4.x );
				coords.add( (float) c4.y );
				coords.add( (float) c4.z );

				m.setBuffer( VertexBuffer.Type.Position, 3, Arrayz.toFloatArray( coords ) );
				//m.setBuffer( VertexBuffer.Type.Index   , 2, Arrayz.toIntArray  ( inds   ) );

				Geometry mg = new Geometry("mesh", m);
				mg.setCullHint(  Spatial.CullHint.Never );
				Material lineMaterial = new Material( tweed.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md" );
				lineMaterial.setColor( "Color", new ColorRGBA( 0, 1f, 1f, 1f ) );
				mg.setMaterial( lineMaterial );

				mg.setLocalTranslation( 0, 0, 0 );
				gNode.attachChild( mg );
			}
		}
	}


	@Override
	public JComponent getUI() {
		return new JLabel("Graph");
	}

}
