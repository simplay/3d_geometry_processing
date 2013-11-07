#version 150

in vec3 curvature_f;
out vec4 out_color;

void main(){                
        float mean = length(curvature_f)/3.0;
        float f = log(1.0 + mean/10.0);
        
        out_color = vec4(f-1.0, f, 1.0-f, 0.0);
        if (f > 1.0) {
                out_color.y = 2.0 - f;
        }
        out_color = clamp(out_color, 0.0, 1.0);
}