use std::error::Error;
use std::fs::{create_dir_all, DirBuilder, File};
use std::io::BufRead;
use std::path::Path;

fn main() -> Result<(), Box<dyn Error>>  {
    let path = Path::new("dofus_db/crs");
    create_dir_all(&path)?;
    
    println!("Hello, world!");
    
    let npath = "dofus_db/crs/test";
    DirBuilder::new().recursive(true).create(npath)?;
    
    let fResult = File::open("test.txt");
    
    let Ok(file) = fResult else {
        panic!("File not found");
    };
    
    let mut reader = std::io::BufReader::new(file);
    let mut line = String::new();
    let len = reader.read_line(&mut line)?;
    println!("Length: {}", len);
    println!("Line: {}", line);
    
    for line in reader.lines() {
        let line = line?;
        println!("Line: {}", line);
    }
    
    Ok(())
}
