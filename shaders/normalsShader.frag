#version 150
// Default fragment shader

// Input variable, passed from vertex to fragment shader
// it is interpolated automatically on each fragment
in vec4 frag_color;
in vec3 normal_o;
in vec3 position_o;
// The output variable, will be written to the display automatically
out vec4 out_color;

void main()
{		
	
	vec3 light_dir = vec3(1.0, 1.0, 0.0);
    vec3 lightDir = normalize(light_dir);
    
    float NdotL = max(dot(normal_o, lightDir), 0.0);
    vec4 lightColor = vec4(1.0, 1.0, 1.0, 1.0);
    
    vec4 diffuseC = frag_color * lightColor * NdotL;
    
    out_color = diffuseC;
    if (NdotL > 0.0) {
       vec3 halfVector = normalize(lightDir - normalize(position_o));
       float HdotN = max(0.0, dot(halfVector,  normal_o));
       vec4 specularC = frag_color * lightColor * pow (HdotN, 5.0);
       out_color += specularC;
    }
	
//	light_dir = normalize(light_dir);
//	out_color = frag_color*(abs(dot(normal_o, light_dir)));	
}
