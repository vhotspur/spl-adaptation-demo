package cz.cuni.mff.d3s.adapt.bookstore.monitor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cz.cuni.mff.d3s.adapt.bookstore.agent.data.EventLogger;
import cz.cuni.mff.d3s.adapt.bookstore.agent.data.LoggedEventListener;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.io.plots.DrawableWriter;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.util.Insets2D;

public class Graphs {
	
	private static class CountedData {
		@SuppressWarnings("unchecked")
		private DataTable data = new DataTable(Long.class, Long.class);
		private long lastTime = 0;
		private long valueAtLastTime = 0;
		
		public synchronized void addConst(long time, int theConst) {
			if (time != lastTime) {
				storeLastTime();
				valueAtLastTime = 0;
			}
			lastTime = time;
			valueAtLastTime += theConst;
		}
		
		public synchronized DataTable getData() {
			return new DataTable(data);
		}
		
		private void storeLastTime() {
			data.add(lastTime, valueAtLastTime);
		}
	}
	
	private static class UniversalListener implements LoggedEventListener {
		private CountedData data;
		private int constant;
		
		public UniversalListener(CountedData data, int constant) {
			this.data = data;
			this.constant = constant;
		}
		
		@Override
		public void action(long timeMillis) {
			data.addConst(timeMillis / 1000, constant);
		}
		
	}
	
	private CountedData violations;

	public Graphs() {
		violations = new CountedData();
		EventLogger.addListener("violation", new UniversalListener(violations, 1));
	}
	
	public InputStream generate(String id) {
		if (id.equals("violations")) {
			return generatePlot(violations.getData());
		}
		
		return null;
	}
	
	public InputStream generatePlot(DataTable data) {
		XYPlot plot = new XYPlot(data);
		
		LineRenderer lines = new DefaultLineRenderer2D();
		plot.setLineRenderer(data, lines);
		plot.setPointRenderer(data, null);
		
		plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_SPACING, 20.0);
		
		plot.setInsets(new Insets2D.Double(20, 60, 60, 40));
		
		DrawableWriterFactory factory = DrawableWriterFactory.getInstance();
		DrawableWriter writer = factory.get("image/png");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			writer.write(plot, out, 1024, 768);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ByteArrayInputStream result = new ByteArrayInputStream(out.toByteArray());
		
		return result;
	}
}
