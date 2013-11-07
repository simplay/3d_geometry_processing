#version 150

uniform mat4 projection;
uniform mat4 modelview;

in vec4 position;
in vec3 curvature;

out vec4 position_g;
out vec3 curvature_g;

void main(){
        position_g = position;
        curvature_g = curvature;        
}