package org.oiue.service.action.http.imageCode;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

@SuppressWarnings({"unused"})
public class ImageCodeServlet extends HttpServlet {
	private static final long serialVersionUID = -6327347468651806863L;
	private Logger logger;
	private static final int WIDTH = 50;
	private static final int HEIGHT = 20;
	private static final int LENGTH = 4;
	public ImageCodeServlet(LogService logService) {
		super();
		this.logger = logService.getLogger(this.getClass());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setHeader("Pragma", "No-cache");
		resp.setHeader("Cache-Control", "no-cache");
		resp.setDateHeader("Expires", 0);
		resp.setContentType("image/jpeg");
		OutputStream out = resp.getOutputStream();

		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Font mFont = new Font("Arial", Font.TRUETYPE_FONT, 18);
		Graphics g = image.getGraphics();
		Random random = new Random();
		// 设置背景颜色
		g.setColor(getRandColor(100, 200));
		g.fillRect(0, 0, WIDTH, HEIGHT);

		// 设置字体
		g.setFont(mFont);

		//设置字体颜色
		g.setColor(getRandColor(180, 250));
		for (int i = 0; i < 555; i++) {
			int x = random.nextInt(WIDTH - 1);
			int y = random.nextInt(HEIGHT - 1);
			int xl = random.nextInt(6) + 1;
			int yl = random.nextInt(12) + 1;
			g.drawLine(x, y, x + xl, y + yl);
		}
		for (int i = 0; i < 450; i++) {
			int x = random.nextInt(WIDTH - 1);
			int y = random.nextInt(HEIGHT - 1);
			int xl = random.nextInt(12) + 1;
			int yl = random.nextInt(6) + 1;
			g.drawLine(x, y, x - xl, y - yl);
		}

		String sRand = "";
		for (int i = 0; i < LENGTH; i++) {
			String tmp = getRandomChar();
			while(tmp.equalsIgnoreCase("0")||tmp.equalsIgnoreCase("o") || tmp.equalsIgnoreCase("1") ||tmp.equalsIgnoreCase("i")|| tmp.equalsIgnoreCase("l") ||tmp.equalsIgnoreCase("z")||tmp.equalsIgnoreCase("2") ){
				tmp = getRandomChar();
			}
			sRand += tmp;
			g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
			g.drawString(tmp, 12 * i + 1, 16);
		}

		HttpSession session = req.getSession(true);
		
		g.dispose();
		try {
//			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//			encoder.encode(image);
			ImageIO.write(image, "jpeg", out);
			session.setAttribute("Login_Image_Code", sRand);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		out.close();
	}
	private String getRandomChar() {
		int rand = (int) Math.round(Math.random() * 2);
		long itmp = 0;
		char ctmp = '\u0000';
		switch (rand) {
		case 1:
			itmp = (long) Math.round(Math.random() * 25 + 97);
			ctmp = (char) itmp;
			return String.valueOf(ctmp);
		default:
			itmp = (long) (Math.random() * 9);
			return String.valueOf(itmp);
		}
	}

	// 设置颜色
	Color getRandColor(int fc, int bc) {
		Random random = new Random();
		fc = fc > 255 ? 255 : fc;
		bc = bc > 255 ? 255 : bc;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doPost(req, resp);
	}

    public void updated(Dictionary<String, ?> props) {
        // TODO Auto-generated method stub
        
    }
}