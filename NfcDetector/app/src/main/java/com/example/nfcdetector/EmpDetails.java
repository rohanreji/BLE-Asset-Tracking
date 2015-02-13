package com.example.nfcdetector;


 
public class EmpDetails {
     
    //private variables
    String _id;
    String _name;
    String _site_name;
    int _project_code;
    String _img_url;
     
    // Empty constructor
    public EmpDetails(){
         
    }
    // constructor
    public EmpDetails(String id, String name,String _site_name, int _project_code,String _img_url){
        this._id = id;
        this._name = name;
        this._site_name=_site_name;
        this._project_code = _project_code;
        this._img_url=_img_url;
        
    }
     
   
    // getting ID
    public String getID(){
        return this._id;
    }
     
    // setting id
    public void setID(String id){
        this._id = id;
    }
     
    // getting name
    public String getName(){
        return this._name;
    }
     
    // setting name
    public void setName(String name){
        this._name = name;
    }
     
 // getting phone number
    public String getSiteName(){
        return this._site_name;
    }
     
    // setting phone number
    void setSiteName(String site_name){
        this._site_name = site_name;
    }
    
    // getting phone number
    public int getProjectCode(){
        return this._project_code;
    }
     
    // setting phone number
    public void setProjectCode(int project_code){
        this._project_code = project_code;
    }
    
    
 // getting phone number
    public String getImageUrl(){
        return this._img_url;
    }
     
    // setting phone number
    public void setImageUrl(String img_url){
        this._img_url = img_url;
    }
}