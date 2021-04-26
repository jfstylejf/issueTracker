package cn.edu.fudan.dependservice.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件复制
 * @author shaoxi
 * @date 20210405
 *
 */
public class DirClone {
//    private String source;
//    private String target;

//    //源路径
    public String source;
    //目标路径
    public String target;

    /**
     * 主函数
     * @param args
     */
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
//        Clone(sourse);
        String source ="D:\\allIdea\\forTest";
        // todo need create dir first
        // todo need create dir first
        String target ="C:\\Users\\admin\\Desktop\\targettt";
        DirClone dirClone =new DirClone(source,target);
        dirClone.creatDir();
        dirClone.Clone(source);
        long endTime = System.currentTimeMillis();
        System.out.println("总共用时："+(endTime-startTime));
    }
    public boolean copy(){
        try {
            long startTime = System.currentTimeMillis();
            creatDir();
            Clone(source);
            long endTime = System.currentTimeMillis();
            System.out.println("总共用时："+(endTime-startTime));
        }catch (Exception e){
            return false;
        }
        return true;


    }
    public void creatDir(){
        File dir = new File(target);
        if (!dir.exists()) {// 判断目录是否存在
            try {
                dir.mkdir();
            }catch (Exception e){
                System.out.println("make dir fail");
                System.out.println(e.getMessage());
            }
        }

    }
    public DirClone(String sourse,String target){
        this.source=sourse;
        this.target=target;

    }

    /**
     * 遍历文件夹并复制
     */
    public void Clone(String url){
        //获取目录下所有文件
        File f = new File(url);
        File[] allf = f.listFiles();

        //遍历所有文件
        for(File fi:allf) {
            try {
                //拼接目标位置
                String URL = target+fi.getAbsolutePath().substring(source.length());

                //创建目录或文件
                if(fi.isDirectory()) {
                    Createflies(URL);
                }else {
                    if(isNeedFile(fi.getName())){
                        fileInputOutput(fi.getAbsolutePath(),URL);
                    }
                        //file.isFile() && file.getName().matches(".*\\.xlsx")
                }

                //递归调用
                if(fi.isDirectory()) {
                    Clone(fi.getAbsolutePath());
                }

            }catch (Exception e) {
                System.out.println("error");
            }
        }
    }
    public boolean isNeedFile(String fileName){
        return fileName.matches(".*\\.java")||
               fileName.matches(".*\\.cpp")||
               fileName.matches(".*\\.hpp")||
               fileName.matches(".*\\.c")||
                fileName.matches(".*\\.h");
    }

    /**
     * 复制文件
     * @param sourse
     * @param target
     */
    public void fileInputOutput(String sourse,String target) {
        try {
            File s = new File(sourse);
            File t = new File(target);

            FileInputStream fin = new FileInputStream(s);
            FileOutputStream fout = new FileOutputStream(t);

            byte[] a = new byte[1024*1024*4];
            int b = -1;

            //边读边写
            while((b = fin.read(a))!=-1) {
                fout.write(a,0,b);
            }

            fout.close();
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建目录
     * @param name
     * @return
     */
    public  boolean Createflies(String name) {
        boolean flag=false;
        File file=new File(name);
        //创建目录
        if(file.mkdir() == true){
            flag=true;
        }else {
            flag=false;

        }

        return flag;
    }
}



