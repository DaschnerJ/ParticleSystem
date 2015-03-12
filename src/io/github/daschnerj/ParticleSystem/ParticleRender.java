package io.github.daschnerj.ParticleSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/*
 * Redniss and Bronowski argued two different points of view. Redniss takes a stand of creativity and trial and error.
 * Bronowski takes a different approach and argues that a structured method is required to make discoveries. These claims
 * are both supported by historical evidence but it could be argued that even though these two methods are finely divided, it could
 * be that both methods go hand in hand in making discoveries and should each be considered for their practical uses.
 * 
 * Trial and error or just mistakingly discovering something helped find things that scientific method may not have found.
 * The scientific method should, instead, be considered an efficient way of discovery and not the only way. So, considering both
 * arguments from Redniss and Bronowski, each method has their merits. This program and code is meant to basically represent this.
 * 
 * Below and in other classes, is the code taken to create this project when run. As seen, the code is very specific and structured 
 * otherwise the project would fail if something was out of place such as a bracket or semicolon. However, this code may look very structured,
 * the output however looks completely random and supports creativity and trial and error. Essentially, both methods are tools and
 * should be used appropriately to make discoveries in the broad field of science.
 * 
 * Controls:
 * 
 * Right Click - Spawn Particles
 * Left Click - Span Gravity Point
 * Left Click + Drag - Attract Particles to Mouse Pointer
 * 
 * c - Clears Particles
 * p - Clears Gravity Points
 * 1 - Increment Color Value
 * 2 - Decrease Color Value
 * 3 - Set Color Value to Default
 * esc - Pause Particles
 * 
 * Resources:
 *  Original Source by Sdurant12 - http://pastebin.com/raw.php?i=DSVWBUzy
 * 
 */

public class ParticleRender extends JPanel implements MouseListener, MouseMotionListener, KeyListener{
    
    Random r = new Random();
    float oldX, oldY;
    
    ArrayList<Particle> particleAL = new ArrayList<Particle>();
    ArrayList<Graviton> gravitonAL = new ArrayList<Graviton>();
    
    private final int WIDTH;
    private final int HEIGHT;
    
    private long lastTime;
    
    private boolean pause = false;
    private boolean emit = false;
    
    private BufferedImage particleImage;
    private int[] particleRaster;
    
    private int[][] densityArray;
    private int[][] lightArray;
    
    private int lightFade = 200;
    private int lightCap = 2048;
    private float lightChange = .5f;
    
    private static int ranAdder = 0;
    public static int colorAdder = 0xff9f1604;
    
    
    public ParticleRender(int W, int H){
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true); 
        requestFocusInWindow();
        
        this.WIDTH = W/2; 
        this.HEIGHT = H/2; 
        
        this.setBackground(Color.BLACK);
        
        particleImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB); 
        particleRaster = ((DataBufferInt)particleImage.getRaster().getDataBuffer()).getData(); 
                
        densityArray = new int[WIDTH/32+1][HEIGHT/32+1]; 
        lightArray = new int[WIDTH/32+1][HEIGHT/32+1];
    }
    
    public void update(Graphics g){
        paint(g);
    }
    
    public void spawnParticles(int ParticleCount){
        
        gravitonAL.add(new Graviton());
        
        int width = WIDTH;
        int height = HEIGHT;
        
        for(int i = 0; i < ParticleCount; i++){
            
            Particle p = new Particle();
            
            float xPos = width/4 + r.nextFloat()*width/2;
            float yPos = height/4 + r.nextFloat()*height/2;
            
            p.setParticle(xPos-150, yPos+100, 0, 0);
            
            particleAL.add(p);
            
        }
    }
    
    private final static float InvSqrt(float x){ 
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x); 
        i = 0x5f3759d5 - (i >> 1); 
        x = Float.intBitsToFloat(i); 
        x = x*(1.5f - xhalf*x*x); 
        return x;
    }
    
    public void glow( int light, int x, int y){ 
        
        if(light > lightCap){
            light = lightCap;
        }
        
        if(lightArray[x][y] <= light){
            lightArray[x][y] = light;
        }
        
        if( light <= lightFade || x <= 0 || x >= (int)(WIDTH/32) || y <= 0 || y >= (int)(HEIGHT/32)){
            
            
        } else {
            
            if( light > lightFade + lightArray[x + 1][y    ] )
                glow( (int)(light*lightChange), x + 1, y    );
                
            if( light > lightFade + lightArray[x - 1][y    ] )
                glow( (int)(light*lightChange), x - 1, y    );
            
            if( light > lightFade + lightArray[x    ][y + 1] )
                glow( (int)(light*lightChange), x   , y  + 1);
            
            if( light > lightFade + lightArray[x    ][y - 1] )
                glow( (int)(light*lightChange), x   , y - 1);
            
        }
        
    }
    
    public int additiveColor(int c1, int c2){ 
        
        int red = (c1 & 0x00ff0000) + (c2 & 0x00ff0000);
        int grn = (c1 & 0x0000ff00) + (c2 & 0x0000ff00);
        int blu = (c1 & 0x000000ff) + (c2 & 0x000000ff);
        
        if( red > 0x00ff0000 )
            red = 0x00ff0000;
        
        if( grn > 0x0000ff00 )
            grn = 0x0000ff00;
        
        if( blu > 0x000000ff )
            blu = 0x000000ff;
        
        return 0xff000000 + red + grn + blu + ranAdder;
    }
    
    public int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000;
        Green = (Green << 8) & 0x0000FF00; 
        Blue = Blue & 0x000000FF; 

        return 0xFF000000 | Red | Green | Blue; 
    }
    
    public int getIntFromColor(float Red, float Green, float Blue){
        int R = Math.round(255 * Red);
        int G = Math.round(255 * Green);
        int B = Math.round(255 * Blue);

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return 0xFF000000 | R | G | B;
    }
    
    public static int randInt(int min, int max) {

        Random rand = new Random();

        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
    
    public void emitParticles(int numberSquare){ 
        
        for(int x = 0; x <= numberSquare; x++){
            for(int y = 0; y <= numberSquare; y++){
                    
                Particle p = new Particle();
                
                float xPos = (oldX + x - numberSquare/2);
                float yPos = (oldY + y - numberSquare/2);
                
                float xVel = (r.nextFloat() - .5f);
                float yVel = (r.nextFloat() - .5f);
                
                p.setParticle(xPos, yPos, xVel, yVel);
                particleAL.add(p);
                    
            }
        }
    }
    
    public void paint(Graphics g){
        super.paintComponent(g);
        float xPos, yPos, xVel, yVel; 
        float ClickToX, ClickToY, InvClickToP; 
        int width = WIDTH; 
        int height = HEIGHT; 
        
        if(emit){
            emitParticles(4);
        }
         
        for(int x_I = 0, lightWidth = (int)(WIDTH/32)+1; x_I < lightWidth; x_I++){  //Draw previous frame's lighting, then clear lightArray
            for(int y_I = 0, lightHeight = (int)(HEIGHT/32)+1; y_I < lightHeight; y_I++){
                
                
                int red = (int)(.15*lightArray[x_I][y_I]);

                if(red > 200){
                    red = 200;
                }
                if(red < 7){
                    red = 0;
                }
                
                g.setColor(new Color((red + ranAdder), (red+ranAdder)/2, (red+ranAdder)/4));
                g.fillRect(x_I*64, y_I*64, 64, 64);
                
                densityArray[x_I][y_I] = (int)(.6*densityArray[x_I][y_I]);

            }
        }
        
        if(!pause){
            
        for(int x_I = 0, lightWidth = (int)(WIDTH/32)+1; x_I < lightWidth; x_I++){  
            for(int y_I = 0, lightHeight = (int)(HEIGHT/32)+1; y_I < lightHeight; y_I++){
                
                lightArray[x_I][y_I] = 0;
                
            }
        }
        
        for(int I = 0; I < particleRaster.length; I++){ 
            particleRaster[I] = 0;
        }
        
        for(int particle_I = 0, pAL = particleAL.size(); particle_I < pAL; particle_I++){
            Particle p = particleAL.get(particle_I);
            
            xPos = p.xPos;
            yPos = p.yPos;
            xVel = p.xVel;
            yVel = p.yVel;
            
            for(int gi = 0, gAL = gravitonAL.size(); gi < gAL; gi++ ){
                
                Graviton v = gravitonAL.get(gi);
                
                ClickToX = v.xPos - xPos;
                ClickToY = v.yPos - yPos;
                float xPull = v.xPull;
                float yPull = v.yPull;
                
                InvClickToP = InvSqrt((ClickToX*ClickToX + ClickToY*ClickToY));
                
                xVel += xPull * ClickToX * InvClickToP; 
                yVel += yPull * ClickToY * InvClickToP; 
                
            }
            
            xPos += xVel; 
            yPos += yVel; 
            
            p.setParticle(xPos, yPos, xVel, yVel); 
            
            if(xPos <= width-4 && xPos >= 4 && yPos <= height-4 && yPos >= 4){ 
                
                for(int xi = -2; xi < 2; xi++){ 
                    for(int yi = -2; yi < 2; yi++){ 
                        
                        particleRaster[(int)(xPos+xi + width*(int)(yPos+yi))] = additiveColor(particleRaster[(int)(xPos+xi + width*(int)(yPos+yi))], colorAdder); // draw particle
                        
                    }
                }
                
            densityArray[ (int)((xPos+2)/32)] [(int)((yPos+2)/32)] += 2;
            }
            
        }
        }
        
        for(int x_I = 0, lightWidth = (int)(WIDTH/32)+1; x_I < lightWidth; x_I++){
            for(int y_I = 0, lightHeight = (int)(HEIGHT/32)+1; y_I < lightHeight; y_I++){
                
                glow(densityArray[x_I][y_I], x_I, y_I);
                
            }
        }
        
        g.drawImage(particleImage, 0,0, 2*WIDTH, 2*HEIGHT, null); 
        
        g.setColor(Color.WHITE);
        g.drawString("Framerate:" + (1000/(System.currentTimeMillis() - lastTime)), 5, 15);
        g.drawString("Particles : " + particleAL.size(), 5, 28);
        g.drawString("Gravity Well : " + gravitonAL.size(), 5, 41);
                
        for(int gi = 0, gAL = gravitonAL.size(); gi < gAL; gi++ ){
               
            Graviton v = gravitonAL.get(gi);
            
            g.fillRect((int)v.xPos*2, 0, 1, 12);
            g.fillRect(0, (int)v.yPos*2, 12, 1);
            g.fillRect((int)v.xPos*2, HEIGHT*2, 1, 12); 
            g.fillRect(WIDTH*2, (int)v.yPos*2, 12, 1); 
           
        }
        lastTime = System.currentTimeMillis();
    }
    
    @Override
    public void mouseClicked(MouseEvent me) {
        if(SwingUtilities.isLeftMouseButton(me)){
            
            float mouseX = me.getX()/2;
            float mouseY = me.getY()/2;
            
            Graviton v = new Graviton();
        
            v.setGraviton(mouseX, mouseY, .5f, .5f);
        
            gravitonAL.add(v);
        }
        
    }

    @Override
    public void mousePressed(MouseEvent me) {}

    @Override
    public void mouseReleased(MouseEvent me) {
        if(SwingUtilities.isRightMouseButton(me)){
            
            emit = false ;
            
        }
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        requestFocusInWindow();
    }

    @Override
    public void mouseExited(MouseEvent me) {
        
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        
        float mouseX = me.getX()/2;
        float mouseY = me.getY()/2;
        
        if(SwingUtilities.isRightMouseButton(me)){
            
            emit = true;
            
        }
        
        if(SwingUtilities.isLeftMouseButton(me)){
            
            Graviton v = new Graviton();
        
            v.setGraviton(mouseX, mouseY, .5f, .5f);
        
            gravitonAL.remove(0);
        
            gravitonAL.add(0, v);
            
        }
        
        oldX = mouseX;
        oldY = mouseY;
        
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        
    }

    @Override
    public void keyTyped(KeyEvent ke) {
        
        int KeyChar = ke.getKeyChar();
        
        System.out.println(KeyChar);
        
        if(KeyChar == 27 /*ESC*/){
            pause = !pause;
        }
        
        if(KeyChar == 112 /*P*/){
            gravitonAL.clear();
            gravitonAL.add(new Graviton());
        }
        
        if(KeyChar == 99 /*C*/){
            particleAL.clear();
        }
        
        if(KeyChar == 49/*1*/)
        {
        	colorAdder = colorAdder + 1;
        }
        
        if(KeyChar == 50/*2*/)
        {
        	colorAdder = colorAdder - 1;
        }
        
        if(KeyChar == 51/*3*/)
        {
        	colorAdder = 0xff9f1604;
        }
        
        if(KeyChar == 52/*4*/)
        {
        	colorAdder = 0;
        }
        
        if(KeyChar == 53/*5*/)
        {
        	colorAdder = 0xffffffff;
        }
        
        
    }

    @Override
    public void keyPressed(KeyEvent ke) {
    	if(ke.isControlDown())
    	{
    		ranAdder = r.nextInt(50);
    	}
    }

    @Override
    public void keyReleased(KeyEvent ke) {}
}


