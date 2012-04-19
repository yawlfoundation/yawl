package org.yawlfoundation.yawl.procletService.editor.block;

public class BlockControl {

  private static BlockControl INSTANCE = null;

  private BlockControl() {
    super();
  }

  public static BlockControl singleton() {
    if (INSTANCE == null) {
      INSTANCE = new BlockControl();
    }
    return INSTANCE;
  }
}
