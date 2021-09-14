package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
@RestController
public class Application {

  static class Self {
    public String href;
  }

  static class Links {
    public Self self;
  }

  static class PlayerState {
    public Integer x;
    public Integer y;
    public String direction;
    public Boolean wasHit;
    public Integer score;
  }

  static class Arena {
    public List<Integer> dims;
    public Map<String, PlayerState> state;
  }

  static class ArenaUpdate {
    public Links _links;
    public Arena arena;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.initDirectFieldAccess();
  }

  @GetMapping("/")
  public String index() {
    return "Let the battle begin!";
  }

  @PostMapping("/**")
  public String index(@RequestBody ArenaUpdate arenaUpdate) {
    // System.out.println(arenaUpdate.toString());

    String myURL = arenaUpdate._links.self.href;
    System.out.println("---\nMy URL : " + myURL);

    Integer[] dims = arenaUpdate.arena.dims.toArray(new Integer[2]);
    int dimX = dims[0];
    int dimY = dims[1];
  
    String[][] arenaMap = new String[dimX][dimY];

    int myX = 0;
    int myY = 0;
    String myDirection = "";

    // Build map
    int playerCount = 0;
    for (Map.Entry<String, PlayerState> state : arenaUpdate.arena.state.entrySet()) {
      String url = state.getKey();
      boolean isMe = url.equals(myURL);
      PlayerState playerState = state.getValue();
      System.out.println("Player " + playerCount + " (me " + isMe + ") URL " + url + " pos" + playerState.x + "," + playerState.y);

      arenaMap[playerState.x][playerState.y] = url;

      if (isMe) {
        myX = playerState.x;
        myY = playerState.y;
        myDirection = playerState.direction;
      }

      playerCount++;
    }

    String command = null;

    command = checkDirection(myDirection, arenaMap, 1, dimX, dimY, myX, myY);
    if (command == null) {
      command = checkDirection(myDirection, arenaMap, 2, dimX, dimY, myX, myY);      
    }
    if (command == null) {
      command = checkDirection(myDirection, arenaMap, 3, dimX, dimY, myX, myY);      
    }

    if (command == null) {
      String[] commands = new String[]{"F", "R", "L"};
      int i = new Random().nextInt(3);
      command = commands[i];
    }
    System.out.println(command);

    return command;
  }

  private String checkDirection(String direction, String[][] arenaMap, int distance, int dimX, int dimY, int myX, int myY) {
    String result = null;

    int targetX = 0;
    int targetY = 0;

    // Is someone in front of me?
    switch (direction) {
      case "N":
        targetX = myX;
        targetY = myY - distance;
        break;
      case "W":
        targetX = myX - distance;
        targetY = myY;
        break;
      case "E":
        targetX = myX + distance;
        targetY = myY;
        break;
      case "S":
        targetX = myX;
        targetY = myY + distance;
        break;
      default:
    }
    System.out.println("Target " + targetX + "," + targetY);

    // target position in bounds?
    if (targetX >= 0 && targetY >= 0 && targetX < dimX && targetY < dimY ) {
      String target = arenaMap[targetX][targetY];
      if (target != null) {
        System.out.println("Target acquired: " + target);
        result = "T";
      }
    }

    return result;
  }

}

