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

  class Vector2 {
    public Vector2(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int x;
    public int y;
  }

  class Direction {

    public String direction;

    Direction(String direction) {
      this.direction = direction;
    }

    public String forward() {
      return this.direction;
    }

    public String left() {
      String result = null;

      switch (this.direction) {
        case "N":
        result = "W";
        break;
      case "W":
        result = "S";
        break;
      case "E":
        result = "N";
        break;
      case "S":
        result = "E";
        break;
      default:
      }      

      return result;
    }

    public String right() {
      String result = null;

      switch (this.direction) {
        case "N":
        result = "E";
        break;
      case "W":
        result = "N";
        break;
      case "E":
        result = "S";
        break;
      case "S":
        result = "W";
        break;
      default:
      }      

      return result;
    }

  }

  Vector2 arenaDim = null;
  String[][] arenaMap = null;
  boolean debug = false;

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
 
    String myURL = arenaUpdate._links.self.href;
    if (debug) { System.out.println("---\nMy URL : " + myURL); }

    Integer[] dims = arenaUpdate.arena.dims.toArray(new Integer[2]);
    arenaDim = new Vector2(dims[0], dims[1]);
  
    arenaMap = new String[arenaDim.x][arenaDim.y];

    PlayerState me = null;

    // Build map
    int playerCount = 0;
    for (Map.Entry<String, PlayerState> state : arenaUpdate.arena.state.entrySet()) {
      String url = state.getKey();
      boolean isMe = url.equals(myURL);
      PlayerState playerState = state.getValue();
      if (debug) { System.out.println("Player " + playerCount + " (me " + isMe + ") URL " + url + " pos" + playerState.x + "," + playerState.y); }

      arenaMap[playerState.x][playerState.y] = url;

      if (isMe) {
        me = playerState;
      }

      playerCount++;
    }

    if (me == null) {
      System.out.println("My URL not found in player list " + myURL);
      return "T";
    }

    if (me.wasHit) {
      // Evasive action

      // Clear path ahead?
      if (evade(me, me.direction)) {
        return "F";
      } else if (evade(me, new Direction(me.direction).right())) {
        return "R";
      } else {
        String[] commands = new String[]{"T", "L"};
        int i = new Random().nextInt(2);
        return commands[i];
      }
    }

    String command = null;
    Vector2 targetLocation = null;

    targetLocation = detect(me, me.direction); // forward
    if (targetLocation != null) {
      command = "T";
    } else {
      targetLocation = detect(me, new Direction(me.direction).left());
      if (targetLocation != null) {
        command = "L";
      } else {
        targetLocation = detect(me, new Direction(me.direction).right());
        if (targetLocation != null) {
          command = "R";
        } else {
          String[] commands = new String[]{"F", "R", "L"};
          int i = new Random().nextInt(3);
          command = commands[i];
        }
      }

    }

    if (debug) { System.out.println(command); }

    return command;
  }

  private Vector2 detect(PlayerState me, String direction) {
    Vector2 result = null;

    Vector2[] targets = null;

    // Is someone in front of me?
    switch (direction) {
      case "N":
        targets = new Vector2[] { new Vector2(me.x, me.y - 1), 
                                  new Vector2(me.x, me.y - 2),
                                  new Vector2(me.x, me.y - 3) };
        break;
      case "W":
        targets = new Vector2[] { new Vector2(me.x - 1, me.y), 
                                  new Vector2(me.x - 2, me.y),
                                  new Vector2(me.x - 3, me.y) };
        break;
      case "E":
        targets = new Vector2[] { new Vector2(me.x + 1, me.y), 
                                  new Vector2(me.x + 2, me.y),
                                  new Vector2(me.x + 3, me.y) };
        break;
      case "S":
        targets = new Vector2[] { new Vector2(me.x, me.y + 1), 
                                  new Vector2(me.x, me.y + 2), 
                                  new Vector2(me.x, me.y + 3) };
        break;
      default:
    }

    String target = null;

    // target position in bounds?
    for (Vector2 v : targets) {
      if (v.x >= 0 && v.y >= 0 && v.x < arenaDim.x && v.y < arenaDim.y ) {
        target = arenaMap[v.x][v.y];
        if (target != null) {
          if (debug) { System.out.println("Target acquired: " + target); }
          result = v;
          break;
        }
      }
    }

    return result;
  }



  private boolean evade(PlayerState me, String direction) {
    boolean result = true; // ok to go that way

    Vector2[] targets = null;

    // Is someone in front of me?
    switch (direction) {
      case "N":
        targets = new Vector2[] { new Vector2(me.x, me.y - 1), 
                                  new Vector2(me.x, me.y - 2) };
        break;
      case "W":
        targets = new Vector2[] { new Vector2(me.x - 1, me.y), 
                                  new Vector2(me.x - 2, me.y) };
        break;
      case "E":
        targets = new Vector2[] { new Vector2(me.x + 1, me.y), 
                                  new Vector2(me.x + 2, me.y) };
        break;
      case "S":
        targets = new Vector2[] { new Vector2(me.x, me.y + 1), 
                                  new Vector2(me.x, me.y + 2) };
        break;
      default:
    }

    String target = null;

    // target position in bounds?
    for (Vector2 v : targets) {
      if (v.x >= 0 && v.y >= 0 && v.x < arenaDim.x && v.y < arenaDim.y ) {
        target = arenaMap[v.x][v.y];
        if (target != null) {
          if (debug) { System.out.println("Target acquired: " + target); }
          result = false;
          break;
        }
      } else {
        result = false;
      }
    }

    return result;
  }

}

