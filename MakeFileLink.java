/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package makefilelink;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * 根据ped文件提取相应sample的绝对路径，并记录至bam.list文件。
 * 由于该case实际情况需要，使用ln -s 生成了相应bam的软链接，
 * 并使用samtools的index命令生成了对应的bam.bai索引文件。
 * @author dell
 */
public class MakeFileLink {
    String bamDir="/home/cy/xiangya_bam";
    String pedFile="/home/jh/projects/RUNER/Autism/ExonCoverage/autism_lmx_moved.ped";
    String targetBamDir="/home/jh/projects/RUNER/Autism/GATKdepth/BamFile";
    String bamListPath="/home/jh/projects/RUNER/Autism/GATKdepth/bamList.txt";
    String samtoolsPath="/sdb1/tools/wgss/samtools/bin/samtools";

    
    public void makeFileLink() throws Exception{
        // get sample id
        Map<String,String> sample= new HashMap<String,String>();
        BufferedReader br= getBufferedReader(pedFile);
        String line=br.readLine();
        while((line=br.readLine())!=null){
            String sampleid=line.split("\t",-1)[1];
            String fid=line.split("\t",-1)[0];
            String samplePath=new File(bamDir).getAbsolutePath()+java.io.File.separator+fid+java.io.File.separator+sampleid+".bam";
            if (!new File(samplePath).exists()){
                System.out.println(sampleid+"\t"+samplePath);
                
                continue;
            }
            sample.put(sampleid,samplePath);
        }
        BufferedWriter bamListFile =getBufferedWriter(bamListPath,false);
        
        for (String sam:sample.keySet()){
            String rawBamPath=sample.get(sam);
            String linkBamPath=targetBamDir+java.io.File.separator+sam+".bam";
            String makeLink="ln -s "+rawBamPath+" "+linkBamPath;//创建软链接
            System.out.println("创建软链接"+rawBamPath);
            Process process=Runtime.getRuntime().exec(makeLink);
            process.waitFor();
            String makeIndex=samtoolsPath+" index "+linkBamPath+" > "+linkBamPath+".bai";//产生index文件
            System.out.println("创建index"+linkBamPath);
            process=Runtime.getRuntime().exec(new String[]{"/bin/sh","-c",makeIndex});//有输出符号>时，需注意
            process.waitFor();
            bamListFile.write(linkBamPath+"\n");
        }
        bamListFile.close();
        br.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)throws Exception {
        // TODO code application logic here
        new MakeFileLink().makeFileLink();
    }
    
    
    public static BufferedReader getBufferedReader(String filePath)throws Exception{
        BufferedReader br = null;
        File dataFile = new File(filePath);
        if(dataFile.exists()){
            br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)));//按行读取指定路径文件
        }else{
            throw new Exception("No input file: " + dataFile.getPath());
        }
        return br;
    }
     static public BufferedWriter getBufferedWriter(String filePath, boolean isGzip) throws Exception {
        BufferedWriter bw = null;
        File dataFile = new File(filePath);
        CharsetEncoder decoder = Charset.forName("UTF-8").newEncoder();
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        if (isGzip) {
            bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(dataFile))));
        } else {
             bw = new BufferedWriter(new FileWriter(dataFile));
        }
        return bw;
    }
    
     
}
