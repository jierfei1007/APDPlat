package com.apdplat.module.security.model;

import com.apdplat.module.module.model.Command;
import com.apdplat.module.module.model.Module;
import com.apdplat.module.module.service.ModuleService;
import com.apdplat.platform.annotation.ModelAttr;
import com.apdplat.platform.generator.ActionGenerator;
import com.apdplat.platform.model.Model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Entity
@Scope("prototype")
@Component
@Table(name = "Role",
uniqueConstraints = {
    @UniqueConstraint(columnNames = {"roleName"})})
@XmlRootElement
@XmlType(name = "Role")
public class Role extends Model {
    @Column(length=40)
    @ModelAttr("角色名")
    protected String roleName;
    @ModelAttr("备注")
    protected String des;

    @ManyToMany(cascade = CascadeType.REFRESH, mappedBy = "roles", fetch = FetchType.LAZY)
    protected List<User> users=new ArrayList<>();

    @ModelAttr("超级管理员")
    protected boolean superManager = false;
    /**
     * 角色拥有的命令
     */
    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "privilege_command", joinColumns = {
    @JoinColumn(name = "privilegeID")}, inverseJoinColumns = {
    @JoinColumn(name = "commandID")})
    @OrderBy("id")
    protected List<Command> commands = new ArrayList<>();
    public String getModuleCommandStr(){
        if(this.commands==null || this.commands.isEmpty()){
            return "";
        }
        StringBuilder ids=new StringBuilder();
        
        Set<Integer> moduleIds=new HashSet<>();
        
        for(Command command : this.commands){
            ids.append("command-").append(command.getId()).append(",");
            Module module=command.getModule();
            moduleIds.add(module.getId());
            module=module.getParentModule();
            while(module!=null){
                moduleIds.add(module.getId());
                module=module.getParentModule();
            }
        }
        for(Integer moduleId : moduleIds){
            ids.append("module-").append(moduleId).append(",");
        }
        ids=ids.deleteCharAt(ids.length()-1);
        return ids.toString();
    }
    /**
     * 璇ヨ鑹插叿鏈夌殑璁块棶鏉冮檺
     * @return
     */
    public List<String> getAuthorities() {
        List<String> result = new ArrayList<>();
        if (superManager) {
            result.add("ROLE_SUPERMANAGER");
        }
        for (Command command : commands) {
            Map<String,String> map=ModuleService.getCommandPathToRole(command);
            for(String role : map.values()){
                StringBuilder str = new StringBuilder();
                str.append("ROLE_MANAGER").append(role);
                result.add(str.toString());
            }
        }
        return result;
    }
 
    @XmlAttribute
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @XmlAttribute
    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    @XmlAttribute
    public boolean isSuperManager() {
        return superManager;
    }

    public void setSuperManager(boolean superManager) {
        this.superManager = superManager;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    @XmlTransient
    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }
  
    public void addCommands(Command command) {
        this.commands.add(command);
    }
  
    public void removeCommand(Command command) {
        this.commands.remove(command);
    }
    public void clearCommand() {
        commands.clear();
    }

    @XmlTransient
    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    @Override
    public String getMetaData() {
        return "角色信息";
    }

    public static void main(String[] args){
        Role obj=new Role();
        //生成Action
        ActionGenerator.generate(obj.getClass());
    }
}
