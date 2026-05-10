-- 1. 根据物料类型的父类型编号查询所有物料类型列表，要求按照排序顺序的升序进行排序，排除停用的信息；
select *  
from md_item_type 
where parent_type_id = 1 and enable_flag =  'Y' 
order by order_num asc

-- 2. 根据物料类型编号查询这个类型以及这个类型的所有子类型下的所有物料列表；按照类型排序顺序升序排序，如果类型相同就按照更新时间的升序排序，排除停用的信息；
WITH recursive type_subtree as (
	select item_type_id,order_num
	from md_item_type
	where item_type_code = 'TYPE01'
			and enable_flag = 'Y'
	union all
	select t.item_type_id,t.order_num
	from md_item_type t
			inner join type_subtree ts on t.parent_type_id = ts.item_type_id
	where t.enable_flag = 'Y'
)
select mi.*
from md_item mi
	inner join type_subtree ts on mi.item_type_id = ts.item_type_id
where mi.enable_flag = 'Y'
order by ts.order_num,mi.update_time;

-- 3. 查询一个产品物料的BOM组成的所有物料列表，要求显示这个物料信息、比例；单位要显示单位名称；排除停用的信息；
select mi.item_code,mi.item_name,mi.specification,bom.quantity,um.measure_name
from md_product_bom bom
	inner join md_item mi on bom.bom_item_id = mi.item_id
	inner join md_item prod on bom.item_id = prod.item_id
	left join md_unit_measure um on bom.unit_of_measure = um.measure_code
where prod.item_code = 'ITEM001'
	and bom.enable_flag = 'Y'
	and mi.enable_flag = 'Y'
	and prod.enable_flag = 'Y';
	
-- 4. 根据物料的编号查询当前不是这个物料的所有BOM组成部分的其他物料；注意要排除当前物料已有的所有BOM组成部分以及有当前物料组成的其他物料；排除停用的信息；
with recursive 
target as (
	select item_id
	from md_item
	where item_code = 'ITEM003' and enable_flag = 'Y'
),
down_tree as(
	select bom.bom_item_id item_id
	from md_product_bom bom
		join target t on bom.item_id = t.item_id
		join md_item mi on bom.bom_item_id = mi.item_id and mi.enable_flag = 'Y'
	where bom.enable_flag = 'Y'
	union all
	select bom.bom_item_id item_id
	from md_product_bom bom
		join down_tree dt on bom.item_id = dt.item_id
		join md_item mi on bom.bom_item_id = mi.item_id and mi.enable_flag = 'Y'
	where bom.enable_flag = 'Y'
),
up_tree as(
	select bom.item_id item_id
	from md_product_bom bom
		join target t on bom.bom_item_id = t.item_id
		join md_item mi on bom.item_id = mi.item_id and mi.enable_flag = 'Y'
	where bom.enable_flag = 'Y'
	union all
	select bom.item_id item_id
	from md_product_bom bom
		join up_tree ut on bom.bom_item_id = ut.item_id
		join md_item mi on bom.item_id = mi.item_id and mi.enable_flag = 'Y'
	where bom.enable_flag = 'Y'
),
related_ids as(
select * from down_tree
union
select * from up_tree
union
select * from target
)
select mi.item_id, mi.item_code, mi.item_name, mi.specification, mi.item_type_name, mi.unit_name
from md_item mi
where mi.enable_flag = 'Y'
	and mi.item_id not in (select item_id from related_ids)
	
-- 5. 根据一个产品物料编码查询组成这个物料产品的所有其他物料的供应商列表，要求显示供应商信息以及对应的物料名称，排除掉已经停用的供应商；
with recursive down_tree as(
	select bom.bom_item_id item_id
	from md_product_bom bom
		join md_item mi on bom.item_id = mi.item_id and mi.enable_flag = 'Y'
	where bom.enable_flag = 'Y' and mi.item_code = 'ITEM001'
	
	union all
	select bom.bom_item_id item_id
	from md_product_bom bom
		join down_tree dt on bom.item_id = dt.item_id
		join md_item mi on bom.bom_item_id = mi.item_id and mi.enable_flag = 'Y'
	where bom.enable_flag = 'Y'
)
select distinct 
    mv.vendor_code        AS 供应商编码,
    mv.vendor_name        AS 供应商名称,
    mv.vendor_nick        AS 供应商简称,
    mv.contact1           AS 联系人,
    mv.contact1_tel       AS 联系电话,
    mv.email              AS 供应商邮箱,
    mi.item_code         AS 物料编码,
    mi.item_name         AS 物料名称,
    mi.specification     AS 物料规格,
    miv.vendor_type       AS 供应类型,
    miv.is_main_vendor    AS 是否主供应商
from down_tree dt
	inner join md_item mi on dt.item_id = mi.item_id and mi.enable_flag = 'Y'
	inner join md_item_vendor miv on dt.item_id = miv.item_id and miv.enable_flag = 'Y'
	inner join md_vendor mv on miv.vendor_id = mv.vendor_id and mv.enable_flag = 'Y'
	
-- 6. 根据一个产品物料编码查询加工这个物料产品对应的所有工作站列表，要求：按照工序步骤升序排序，如果工序步骤相同的按照工作站ID升序排序；
-- 要求显示所有的工序名称、工作站信息、车间名称；排除停用的信息；
select mi.item_name,mps.process_id,mps.process_name,mw.workstation_id,mw.workstation_name,mw.workshop_id,mw.workshop_name
from md_item mi 
	inner join md_product_sop mps on mi.item_id = mps.item_id
	inner join md_workstation mw on mps.process_id = mw.process_id and mw.enable_flag = 'Y'
where mi.item_code = 'ITEM001' and mi.enable_flag = 'Y'
order by mps.order_num,mw.workstation_id

-- 7. 统计每个工作站的用工数量，要求显示工作站信息，车间名称，满负荷情况下的用工数量和175%比例的用工数量；
-- 注意：计算后的用工数量要向上取整，不能出现小数；排除停用的信息；
select mw.workstation_id,mw.workstation_name,mw.workshop_name,coalesce(sum(mww.quantity),0) as '满负荷用工数量',ceil(coalesce(sum(mww.quantity),0)*1.75) as '175%比例用工数量'
from md_workstation mw
	left join md_workstation_worker mww on mw.workstation_id = mww.workstation_id
	inner join md_workshop shop on mw.workshop_id = shop.workshop_id and shop.enable_flag = 'Y'
where mw.enable_flag = 'Y'
group by mw.workstation_id,mw.workstation_name,mw.workshop_name

-- 8. 统计每个物料类型下的所有物料产品总数量；排除停用的信息；
select mit.item_type_id,mit.item_type_name,count(mi.item_id)
from md_item_type mit
	left join md_item mi on mit.item_type_id = mi.item_type_id and mi.enable_flag = 'Y'
where mit.enable_flag = 'Y'
group by mit.item_type_id,mit.item_type_name

-- 9. 根据设备编号查询这个设备所在的所有工作站和车间；按照车间编号升序排序；车间相同的按照工作站编号升序排序； 排除停用信息；
select mwm.machinery_code,mwm.machinery_name,mw.workstation_code,mw.workstation_name,mw.workshop_name
from md_workstation_machine mwm 
	inner join md_workstation mw on mwm.workstation_id = mw.workstation_id and enable_flag = 'Y'
	inner join md_workshop shop on mw.workshop_id = shop.workshop_id and shop.enable_flag = 'Y'
where mwm.machinery_code = 'M001'
order by mw.workshop_code,mw.workstation_code

-- 10. 按季度统计2025年新增加的客户数量，要求表头为：第一季度、第二季度、第三季度、第四季度；
select 
	sum(case when quarter(create_time) = 1 then 1 else 0 end) as '第一季度',
	sum(case when quarter(create_time) = 2 then 1 else 0 end) as '第二季度',
	sum(case when quarter(create_time) = 3 then 1 else 0 end) as '第三季度',
	sum(case when quarter(create_time) = 4 then 1 else 0 end) as '第四季度'
from md_client
where year(create_time) = 2025



