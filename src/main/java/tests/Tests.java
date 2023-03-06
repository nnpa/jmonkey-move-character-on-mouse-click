package tests;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Ray;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
/**
 * This is the Main Class of your Game. It should boot up your game and do initial initialisation
 * Move your Logic into AppStates or Controls or other java classes
 */
public class Tests extends SimpleApplication   {
private Spatial player;
 private Node shootables;
  private Geometry mark;
  CharacterControl characterControl;
    public static void main(String[] args) {
        Tests app = new Tests();
        app.setShowSettings(false); //Settings dialog not supported on mac
        app.start();
    }
    
  private void initMark() {
    Sphere sphere = new Sphere(30, 30, 0.2f);
    mark = new Geometry("BOOM!", sphere);
    Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mark_mat.setColor("Color", ColorRGBA.Red);
    mark.setMaterial(mark_mat);
  }

  private void initKeys() {
    inputManager.addMapping("Shoot",
      new KeyTrigger(KeyInput.KEY_SPACE), // trigger 1: spacebar
      new MouseButtonTrigger(MouseInput.BUTTON_LEFT)); // trigger 2: left-button click
    inputManager.addListener(actionListener, "Shoot");
  }
      
  final private ActionListener actionListener = new ActionListener() {
    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("Shoot") && !keyPressed) {
        Vector3f origin = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);

        Vector3f direction = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.3f);

        direction.subtractLocal(origin).normalizeLocal();

        CollisionResults results = new CollisionResults();

        Ray ray = new Ray( cam.getLocation() , direction );



        shootables.collideWith( ray, results );

        if( results.size() > 0 ){

          CollisionResult closest = results.getClosestCollision();
          // Let's interact - we mark the hit with a red dot.
          Vector3f contactPoint = closest.getContactPoint();
          mark.setLocalTranslation(contactPoint);
          rootNode.attachChild(mark);
          Vector3f  correction = new Vector3f(contactPoint.x,contactPoint.y+1,contactPoint.z);
          
          
          moveTo = correction;
            
        }
      }
    }
  };
  
@Override
  public void simpleInitApp() {
    shootables = new Node("Shootables");
    rootNode.attachChild(shootables);
    
    setUpLight();
    initKeys();
    initMark();
    
    //init physics
    BulletAppState bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    

    //ini player
    player = (Spatial) assetManager.loadModel("Models/char.glb");
    rootNode.attachChild(player);
    
//init player physics

    SphereCollisionShape sphereShape = new SphereCollisionShape(1.2f);
    characterControl = new CharacterControl( sphereShape , 1.2f );
    player.addControl(characterControl);
    characterControl.setPhysicsLocation(new Vector3f(0,2f,0));
    rootNode.attachChild(player);
     bulletAppState.getPhysicsSpace().add(characterControl);
    
    //init flor
    Spatial    flor = assetManager.loadModel("Models/flor.glb");
    shootables.attachChild(flor);
    //init flor physics
    CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(flor);
    RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
    flor.addControl(landscape);
    bulletAppState.getPhysicsSpace().add(landscape);
    

        
        
    // Disable the default first-person cam!
    flyCam.setEnabled(false);

    // Enable a chase cam
    ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager);

    //Uncomment this to invert the camera's vertical rotation Axis 
    //chaseCam.setInvertVerticalAxis(true);

    //Uncomment this to invert the camera's horizontal rotation Axis
    //chaseCam.setInvertHorizontalAxis(true);

    //Comment this to disable smooth camera motion
    chaseCam.setSmoothMotion(true);

  }

  private void setUpLight() {

        
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
        
        DirectionalLight dl2 = new DirectionalLight();
        dl2.setColor(ColorRGBA.White);
       dl2.setDirection(new Vector3f(100,3,26).normalizeLocal());
       rootNode.addLight(dl2);
       
       
    }
  private Vector3f moveTo = null;
  
  private Vector3f  walkDirection = new Vector3f(0,0,0);

  @Override
  public void simpleUpdate(float tpf) {
    walkDirection.set(0, 0, 0);
    if(moveTo != null){
        
        //set loock at mark
        characterControl.setViewDirection(mark.getLocalTranslation());
        //get player position
        Vector3f playerPosition = characterControl.getPhysicsLocation();
        float distance = playerPosition.distance(moveTo);
        if(distance > 0.2f){
            Vector3f direction = moveTo.subtract(playerPosition).normalize();
            walkDirection.addLocal(direction);
            characterControl.setWalkDirection(walkDirection.mult(0.3f));
        }
    }

  }
}
