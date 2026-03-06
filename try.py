import openpyxl

def clear_excel_data(file_path, start_row=5):
    try:
        # 加载工作簿
        workbook = openpyxl.load_workbook(file_path)
        
        for sheet in workbook.worksheets:
            # 获取当前 sheet 的最大行数
            max_row = sheet.max_row
            
            # 如果最大行数小于开始行，则无需操作
            if max_row < start_row:
                print(f"工作表 '{sheet.title}' 行数不足 {start_row} 行，已跳过。")
                continue
            
            # 从最后一行开始向上删除，直到 start_row
            # delete_rows(idx, amount) idx 是起始行，amount 是删除多少行
            rows_to_delete = max_row - start_row + 1
            sheet.delete_rows(start_row, rows_to_delete)
            
            print(f"工作表 '{sheet.title}' 已清理，删除了从第 {start_row} 行起的 {rows_to_delete} 行数据。")

        # 保存修改
        workbook.save(file_path)
        print(f"\n操作完成！文件已保存至: {file_path}")

    except Exception as e:
        print(f"处理过程中出现错误: {e}")

if __name__ == "__main__":
    # 在这里输入你的 Excel 文件路径
    target_file = "C:\Users\gaobaizhou\Desktop\数据库课设\数据源.xlsx"
    clear_excel_data(target_file)