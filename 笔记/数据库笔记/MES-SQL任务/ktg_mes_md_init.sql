-- ----------------------------
-- 清空旧数据（安全）
-- ----------------------------
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE md_item_type;
TRUNCATE md_item;
TRUNCATE md_product_bom;
TRUNCATE md_item_batch_config;
TRUNCATE md_vendor;
TRUNCATE md_item_vendor;
TRUNCATE md_client;
TRUNCATE md_unit_measure;
TRUNCATE md_workshop;
TRUNCATE md_workstation;
TRUNCATE md_workstation_machine;
TRUNCATE md_workstation_worker;
TRUNCATE md_workstation_tool;
TRUNCATE md_product_sop;
TRUNCATE md_product_sip;
SET FOREIGN_KEY_CHECKS=1;

-- ----------------------------
-- 1. 物料类型
-- ----------------------------
INSERT INTO md_item_type (item_type_id,item_type_code,item_type_name,parent_type_id,ancestors,item_or_product,order_num,enable_flag,remark,attr1,attr2,attr3,attr4,create_by,create_time,update_by,update_time) VALUES
(1,'TYPE01','成品',0,'0','ITEM',1,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(2,'TYPE02','原材料',1,'0,1','ITEM',2,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(3,'TYPE03','电子元件',2,'0,1,2','ITEM',3,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(4,'TYPE04','半成品',1,'0,1','ITEM',4,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(5,'TYPE05','结构件',2,'0,1,2','ITEM',5,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(6,'TYPE06','包装材料',2,'0,1,2','ITEM',6,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW());

-- ----------------------------
-- 2. 物料
-- ----------------------------
INSERT INTO md_item (item_id,item_code,item_name,specification,unit_of_measure,unit_name,item_or_product,item_type_id,item_type_code,item_type_name,enable_flag,safe_stock_flag,min_stock,max_stock,high_value,batch_flag,remark,attr1,attr2,attr3,attr4,create_by,create_time,update_by,update_time) VALUES
(1,'ITEM001','手机整机','6.7英寸','PC','个','ITEM',1,'TYPE01','成品','Y','N',0,0,'N','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(2,'ITEM002','手机屏幕','6.7英寸','PC','个','ITEM',3,'TYPE03','电子元件','Y','N',0,0,'N','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(3,'ITEM003','手机电池','5000mAh','PC','个','ITEM',3,'TYPE03','电子元件','Y','N',0,0,'N','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(4,'ITEM004','手机后盖','玻璃','PC','个','ITEM',5,'TYPE05','结构件','Y','N',0,0,'N','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(5,'ITEM005','摄像头模组','1亿像素','PC','个','ITEM',3,'TYPE03','电子元件','Y','N',0,0,'N','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(6,'ITEM006','包装盒','标准彩盒','PC','个','ITEM',6,'TYPE06','包装材料','Y','N',0,0,'N','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(7,'ITEM007','平板电脑','10.9英寸','PC','台','ITEM',1,'TYPE01','成品','Y','N',0,0,'N','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(8,'ITEM008','平板屏幕','10.9英寸','PC','个','ITEM',3,'TYPE03','电子元件','Y','N',0,0,'N','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(9,'ITEM009','平板电池','8000mAh','PC','个','ITEM',3,'TYPE03','电子元件','Y','N',0,0,'N','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW());

-- ----------------------------
-- 3. BOM
-- ----------------------------
INSERT INTO md_product_bom (bom_id,item_id,bom_item_id,bom_item_code,bom_item_name,bom_item_spec,unit_of_measure,item_or_product,quantity,enable_flag,remark,attr1,attr2,attr3,attr4,create_by,create_time,update_by,update_time) VALUES
(1,1,2,'ITEM002','手机屏幕','6.7英寸','PC','ITEM',1,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(2,1,3,'ITEM003','手机电池','5000mAh','PC','ITEM',1,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(3,1,4,'ITEM004','手机后盖','玻璃','PC','ITEM',1,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(4,1,5,'ITEM005','摄像头模组','1亿','PC','ITEM',1,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(5,1,6,'ITEM006','包装盒','彩盒','PC','ITEM',1,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(6,7,8,'ITEM008','平板屏幕','10.9','PC','ITEM',1,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(7,7,9,'ITEM009','平板电池','8000mAh','PC','ITEM',1,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(8,7,6,'ITEM006','包装盒','彩盒','PC','ITEM',1,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW());

-- ----------------------------
-- 4. 供应商
-- ----------------------------
INSERT INTO md_vendor (vendor_id,vendor_code,vendor_name,vendor_nick,vendor_en,vendor_des,vendor_logo,vendor_level,vendor_score,address,website,email,tel,contact1,contact1_tel,contact1_email,contact2,contact2_tel,contact2_email,credit_code,enable_flag,remark,attr1,attr2,attr3,attr4,create_by,create_time,update_by,update_time) VALUES
(1,'V001','京东方','京东方','','','','A',100,'','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(2,'V002','比亚迪','比亚迪','','','','A',98,'','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(3,'V003','富士康','富士康','','','','A',99,'','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(4,'V004','立讯精密','立讯','','','','A',97,'','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(5,'V005','歌尔股份','歌尔','','','','A',96,'','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW());

-- ----------------------------
-- 5. 物料供应商关系
-- ----------------------------
INSERT INTO md_item_vendor (id,item_id,item_code,item_name,vendor_id,vendor_code,vendor_name,vendor_type,is_main_vendor,enable_flag,remark,create_by,create_time,update_by,update_time) VALUES
(1,2,'ITEM002','手机屏幕',1,'V001','京东方','主料','Y','Y','','admin',NOW(),'admin',NOW()),
(2,3,'ITEM003','手机电池',2,'V002','比亚迪','主料','Y','Y','','admin',NOW(),'admin',NOW()),
(3,4,'ITEM004','手机后盖',3,'V003','富士康','主料','Y','Y','','admin',NOW(),'admin',NOW()),
(4,5,'ITEM005','摄像头模组',4,'V004','立讯精密','主料','Y','Y','','admin',NOW(),'admin',NOW()),
(5,6,'ITEM006','包装盒',5,'V005','歌尔股份','主料','Y','Y','','admin',NOW(),'admin',NOW()),
(6,8,'ITEM008','平板屏幕',1,'V001','京东方','主料','Y','Y','','admin',NOW(),'admin',NOW()),
(7,9,'ITEM009','平板电池',2,'V002','比亚迪','主料','Y','Y','','admin',NOW(),'admin',NOW());

-- ----------------------------
-- 6. 车间
-- ----------------------------
INSERT INTO md_workshop (workshop_id,workshop_code,workshop_name,area,charge,enable_flag,remark,attr1,attr2,attr3,attr4,create_by,create_time,update_by,update_time) VALUES
(1,'WS001','组装车间',100,'张三','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(2,'WS002','测试车间',200,'李四','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(3,'WS003','包装车间',150,'王五','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW());

-- ----------------------------
-- 7. 工作站
-- ----------------------------
INSERT INTO md_workstation (workstation_id,workstation_code,workstation_name,workstation_address,workshop_id,workshop_code,workshop_name,process_id,process_code,process_name,warehouse_id,warehouse_code,warehouse_name,location_id,location_code,location_name,area_id,area_code,area_name,enable_flag,remark,attr1,attr2,attr3,attr4,create_by,create_time,update_by,update_time) VALUES
(1,'W001','屏幕组装站','一楼',1,'WS001','组装车间',1,'P001','屏幕工序',1,'WH001','线边库1',1,'L001','库区1',1,'A001','库位1','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(2,'W002','电池组装站','一楼',1,'WS001','组装车间',2,'P002','电池工序',1,'WH001','线边库1',1,'L001','库区1',1,'A001','库位1','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(3,'W003','后盖组装站','一楼',1,'WS001','组装车间',3,'P003','后盖工序',1,'WH001','线边库1',1,'L001','库区1',1,'A001','库位1','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(4,'W004','功能测试站','二楼',2,'WS002','测试车间',4,'P004','测试工序',1,'WH001','线边库1',1,'L001','库区1',1,'A001','库位1','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(5,'W005','包装工作站','三楼',3,'WS003','包装车间',5,'P005','包装工序',1,'WH001','线边库1',1,'L001','库区1',1,'A001','库位1','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(6,'W006','平板组装站','一楼',1,'WS001','组装车间',6,'P006','平板组装',1,'WH001','线边库1',1,'L001','库区1',1,'A001','库位1','Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW());

-- ----------------------------
-- 8. 人力资源
-- ----------------------------
INSERT INTO md_workstation_worker (record_id,workstation_id,post_id,post_code,post_name,quantity,remark,attr1,attr2,attr3,attr4,create_by,create_time,update_by,update_time) VALUES
(1,1,1,'POST01','装配工',2,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(2,2,1,'POST01','装配工',2,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(3,3,1,'POST01','装配工',3,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(4,4,2,'POST02','测试员',2,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(5,5,3,'POST03','包装工',2,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(6,6,1,'POST01','装配工',4,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW());

-- ----------------------------
-- 9. 设备
-- ----------------------------
INSERT INTO md_workstation_machine (record_id,workstation_id,machinery_id,machinery_code,machinery_name,quantity,remark,attr1,attr2,attr3,attr4,create_by,create_time,update_by,update_time) VALUES
(1,1,101,'M001','贴片机',1,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(2,2,102,'M002','锁螺丝机',1,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(3,3,103,'M003','热压机',1,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(4,4,104,'M004','测试机',1,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(5,5,105,'M005','封箱机',1,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(6,6,101,'M001','贴片机',1,'',NULL,NULL,0,0,'admin',NOW(),'admin',NOW());

-- ----------------------------
-- 10. SOP
-- ----------------------------
INSERT INTO md_product_sop (sop_id,item_id,order_num,process_id,process_code,process_name,sop_title,sop_description,sop_url,remark,attr1,attr2,attr3,attr4,create_by,create_time,update_by,update_time) VALUES
(1,1,1,1,'P001','屏幕工序','贴屏幕','对齐贴紧','','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(2,1,2,2,'P002','电池工序','装电池','固定电池','','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(3,1,3,3,'P003','后盖工序','装后盖','压紧','','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(4,1,4,4,'P004','测试工序','功能测试','通电检查','','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(5,1,5,5,'P005','包装工序','装箱打包','封箱','','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(6,7,1,6,'P006','平板组装','装屏幕','贴合','','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(7,7,2,4,'P004','测试工序','平板测试','整机测试','','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW());

-- ----------------------------
-- 11. 客户 2025 
-- ----------------------------
INSERT INTO md_client (client_id,client_code,client_name,client_nick,client_en,client_des,client_logo,client_type,address,website,email,tel,contact1,contact1_tel,contact1_email,contact2,contact2_tel,contact2_email,credit_code,enable_flag,remark,attr1,attr2,attr3,attr4,create_by,create_time,update_by,update_time) VALUES
(1,'C001','客户A','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-01-10','admin',NOW()),
(2,'C002','客户B','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-04-10','admin',NOW()),
(3,'C003','客户C','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-07-10','admin',NOW()),
(4,'C004','客户D','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-10-10','admin',NOW()),
(5,'C005','客户E','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-01-15','admin',NOW()),
(6,'C006','客户F','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-02-10','admin',NOW()),
(7,'C007','客户G','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-04-12','admin',NOW()),
(8,'C008','客户H','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-05-20','admin',NOW()),
(9,'C009','客户I','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-07-15','admin',NOW()),
(10,'C010','客户J','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-08-10','admin',NOW()),
(11,'C011','客户K','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-10-11','admin',NOW()),
(12,'C012','客户L','','','','','ENTERPRISE','','','','','','','','','','','','Y','',NULL,NULL,0,0,'admin','2025-11-25','admin',NOW());

-- ----------------------------
-- 12. 单位
-- ----------------------------
INSERT INTO md_unit_measure (measure_id,measure_code,measure_name,primary_flag,primary_id,change_rate,enable_flag,remark,attr1,attr2,attr3,attr4,create_by,create_time,update_by,update_time)
VALUES 
(1,'PC','个','Y',NULL,1.0000,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW()),
(2,'SET','台','Y',NULL,1.0000,'Y','',NULL,NULL,0,0,'admin',NOW(),'admin',NOW());