package com.alifi.mizar.vo;

/**
 * 下载参数设置
 * @author tongpeng.chentp
 * @since 2.0.1
 */
public class FileParam {
    
    /**
     * 下载的文件名设置
     */
    private String contentDisposition;
    
    /**
     * 下载的内容
     */
    private byte[] bytes;

    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }
    
    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
