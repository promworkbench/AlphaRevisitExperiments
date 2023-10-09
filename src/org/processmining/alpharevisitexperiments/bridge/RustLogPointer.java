package org.processmining.alpharevisitexperiments.bridge;

public class RustLogPointer {
    private final long pointer;

    public RustLogPointer(long pointer) {
        this.pointer = pointer;
    }

    public void destroy() {
        RustBridge.destroyRustEventLogWrapper(this.pointer);
        System.out.println("Destroyed Rust Event Log Wrapper with pointer " + this.pointer);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.destroy();
    }

}
