ALTER TABLE OH_MEDICALDSRTYPE 
ADD COLUMN MDSRT_DELETED CHAR(1) NOT NULL DEFAULT 'N' AFTER MDSR_LOCK;