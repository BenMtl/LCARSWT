package de.tucottbus.kt.lcars.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.jfree.experimental.swt.SWTUtils;

import de.tucottbus.kt.lcars.PanelState;
import de.tucottbus.kt.lcars.elements.ElementData;

public abstract class ElementDataCanvas extends Canvas implements PaintListener
{    
  private ElementData ed;
  private PanelState ps;
    
  private final Display display;
    
  public ElementDataCanvas(Composite parent, int style) {
    super(parent, style);
    display = parent.getDisplay();
    addPaintListener(this);
  }
  
  @Override
  public void paintControl(PaintEvent e)
  {
    ElementData elData = ed;
    PanelState pState = ps;
    if (elData == null) return;    
    assert(pState != null);
            
    //TODO: Add global transform
    GC gc = e.gc;
    
    Rectangle bounds = getBounds();
    Transform t = new Transform(gc.getDevice(), 0, 0, 0, 0, -bounds.x, -bounds.y);
    Transform ot = new Transform(gc.getDevice());
    gc.getTransform(t);
    gc.getTransform(ot);
    gc.setTransform(t);        
      
    prePaint(gc);
    elData.render2D(gc, pState);        
    postPaint(gc);
    
    gc.setTransform(ot);
    t.dispose();
    ot.dispose();        
  }
  
  public void applyUpdate(ElementData elementData, PanelState panelState) {
    assert (elementData != null) : "elementData != null";
    assert (ed == null || elementData.serialNo == ed.serialNo);
    int edUpdate = elementData.applyUpdate(this.ed);
    ed = elementData;
    
    boolean redraw = edUpdate != 0;
    
    if (panelState != null) {
      this.ps = panelState;  
      redraw = true;
    }
    
    if ((edUpdate & ElementData.GEOMETRY_FLAG) > 0) // new bounds
    {
      Rectangle bounds = SWTUtils.toSwtRectangle(elementData.getBounds());      
      display.asyncExec(redraw
          ? () -> {          
              setBounds(bounds);        
              redraw();
            }
          : () -> { setBounds(bounds); } 
          );      
    }
    else
      if (redraw)
        display.asyncExec(() -> { redraw(); });      
  }
  
  public void clear() {
    ed = null;
    ps = null;
  }
      
  @Override
  public void setVisible(boolean visible) {
    if(visible == getVisible()) return;
    super.setVisible(visible);
    ed.onVisibilityChanged(visible);    
  }
    
  public abstract void prePaint(GC gc);
  public abstract void postPaint(GC gc);  
}
