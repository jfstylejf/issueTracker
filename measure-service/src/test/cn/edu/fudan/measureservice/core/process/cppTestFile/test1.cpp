#include "issue.h"
#include "string"
using namespace std;



const int global_1;
float global_2;
vector<int> global_3;

class Stats {
private:
    double start_;
    double finish_;
    double seconds_;
    int done_;
    double last_op_finish_;

public:
    void Start(int a,int b) {
        last_op_finish_ = start_;
        done_ = 0;
        seconds_ = 0;
        finish_ = start_;
    }
    static void Start2() {
       int a = 0;
    }
    static void Start3() {
        string s = "123";
    }
};