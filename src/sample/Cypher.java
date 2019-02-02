package sample;

import java.math.BigInteger;
import java.util.Random;

public class Cypher {
    static boolean needGenNewKey=false;
    boolean GenModIsGenerate=false;
    boolean setOtherKeyB=false;
    public BigInteger gen;
    public BigInteger modul;
    public BigInteger privateKey;
    public BigInteger publicKey;
    public BigInteger otherKey;
    public BigInteger password;
    public String passwordString;
    private int keysSize=1024;//1024 бит
    //private String salt = KeyGenerators.string().generateKey();
    public String salt="324fee14e5b58635";

    public Cypher(){
        System.out.println("КЛАСС");
        Random random = new Random();
        privateKey=new BigInteger(keysSize,random);//54
    }

    public String genGenMod(){
        System.out.println("КЛАСС");
        Random random = new Random();
        gen=new BigInteger(keysSize,random);
        modul=new BigInteger(keysSize,random);
        publicKey=gen.modPow(privateKey,modul);
        GenModIsGenerate=true;
        return gen+" "+modul+" "+publicKey;
    }

    public String setGenMod(String str){
        System.out.println("КЛАСС");
        System.out.println("Приняли ключи");
        System.out.println(str);
        String[] strt = new String[2];
        int p1=0;
        int p2=0;
        for(int i=1;i<str.length();i++){
            if(str.substring(i-1,i).equals(" ")){
                strt[p1]=str.substring(p2,i-1);
                //System.out.println(str.substring(p2,i-1));
                p2=i;
                p1++;
            }
        }
        gen= new BigInteger(strt[0]);
        modul = new BigInteger(strt[1]);
        otherKey= new BigInteger(str.substring(p2,str.length()));


        publicKey=gen.modPow(privateKey,modul);


        GenModIsGenerate=true;
        setOtherKeyB=true;
        genPassword();
        return String.valueOf(publicKey);
    }

    public void setOtherKey(String str){
        System.out.println("класс Приняли публичный ключ ключ другого клиента");
        System.out.println(">>>"+str);
        otherKey= new BigInteger(str);
        setOtherKeyB=true;
        genPassword();
    }

    private void genPassword(){
        password=otherKey.modPow(privateKey,modul);
        System.out.println("----------------------------");
        System.out.println("gen = "+gen);
        System.out.println("mod = "+modul);
        System.out.println("other key = "+otherKey);
        System.out.println("my public key = "+publicKey);
        System.out.println("password dec= "+password );
       /* String passBit=""+password.toString(2);
        System.out.println("password binary= "+passBit);
        String codeText= new String(new BigInteger(passBit, 2).toByteArray());
        codeText=codeText.replaceAll("\\s+","");
        passwordStr=codeText.substring(0,32);
        System.out.println("password char = "+passwordStr);*/

        String passStr=""+password.toString(2);
        String codeText= new String(new BigInteger(passStr, 2).toByteArray());
        codeText=codeText.replaceAll("\\s+","");
        //codeText=codeText.replaceAll("[^a-zA-Z0-9!@#$%^&*()_+№;:?<>,./'|]","");
        //codeText=codeText.replaceAll("[^!-ÿ]","");
        codeText=codeText.replaceAll("[^a-zA-Z0-9!@#$%^&*()_+;:?]","");
        if(codeText.length()>32) codeText=codeText.substring(0,32);
        passwordString=codeText;
        System.out.println("Final password = "+passwordString+": "+passwordString.length());
        //System.out.println("----------------------------");




    }
}
